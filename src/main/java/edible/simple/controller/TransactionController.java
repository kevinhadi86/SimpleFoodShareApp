/**
 * Alipay.com Inc.
 * Copyright (c) 2004‐2019 All Rights Reserved.
 */
package edible.simple.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edible.simple.model.*;
import edible.simple.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edible.simple.model.dataEnum.StatusEnum;
import edible.simple.payload.ApiResponse;
import edible.simple.payload.offer.OtherUserOfferResponse;
import edible.simple.payload.transcation.AddTransactionRequest;
import edible.simple.payload.transcation.TransactionResponse;
import edible.simple.payload.transcation.UpdateTransactionStatusRequest;
import edible.simple.payload.user.BaseUserResponse;
import edible.simple.security.CurrentUser;
import edible.simple.security.UserPrincipal;

/**
 * @author Kevin Hadinata
 * @version $Id: TransactionController.java, v 0.1 2019‐09‐22 22:04 Kevin Hadinata Exp $$
 */
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @Autowired
    UserService        userService;

    @Autowired
    OfferService       offerService;

    @Autowired
    UnitService        unitService;

    @Autowired
    ReviewService      reviewService;

    static Logger      logger = LoggerFactory.getLogger(TransactionController.class);

    @GetMapping("/myTransaction/take")
    public List<TransactionResponse> getMyTransactionAsTaker(@CurrentUser UserPrincipal userPrincipal) {

        User user = userService.getUserById(userPrincipal.getId());

        List<Transaction> transactions = transactionService.getTransactionByUser(user);
        List<TransactionResponse> myTransaction = new ArrayList<>();

        Date now = new Date();
        logger.info(now.toString());
        for (Transaction transaction : transactions) {

            if ((!transaction.getStatus().equals(StatusEnum.EXPIRED)
                 || !(transaction.getStatus().equals(StatusEnum.DONE))
                 || !(transaction.getStatus().equals(StatusEnum.REVIEWED)))
                && transaction.getPickupTime().before(now)) {
                transaction.setStatus(StatusEnum.EXPIRED);
                transactionService.saveTransaction(transaction);
            }

            TransactionResponse transactionResponse = new TransactionResponse();

            transactionResponse.setId(transaction.getId());
            transactionResponse.setStatus(transaction.getStatus().name());
            transactionResponse.setUnit(transaction.getUnit().getUnitName().name());
            transactionResponse.setQuantity(transaction.getQuantity());
            transactionResponse
                .setPickupTime(new SimpleDateFormat("HH:mm").format(transaction.getPickupTime()));

            BaseUserResponse userResponse = new BaseUserResponse();
            BeanUtils.copyProperties(transaction.getUser(), userResponse);

            transactionResponse.setUser(userResponse);

            OtherUserOfferResponse otherUserOfferResponse = new OtherUserOfferResponse();
            fillOfferResponse(otherUserOfferResponse, transaction);

            transactionResponse.setOffer(otherUserOfferResponse);

            myTransaction.add(transactionResponse);
        }
        return myTransaction;
    }

    @GetMapping("/myTransaction")
    public List<TransactionResponse> getMyTransaction(@CurrentUser UserPrincipal userPrincipal) {

        User user = userService.getUserById(userPrincipal.getId());

        List<Transaction> transactions = transactionService.getByOfferUser(user);
        List<TransactionResponse> myTransaction = new ArrayList<>();

        Date now = new Date();
        logger.info(now.toString());
        for (Transaction transaction : transactions) {

            if ((!transaction.getStatus().equals(StatusEnum.EXPIRED)
                 || !(transaction.getStatus().equals(StatusEnum.DONE))
                 || !(transaction.getStatus().equals(StatusEnum.REVIEWED)))
                && transaction.getPickupTime().before(now)) {
                transaction.setStatus(StatusEnum.EXPIRED);
                transactionService.saveTransaction(transaction);
            }

            if (transaction.getOffer().getUser().equals(user)) {
                TransactionResponse transactionResponse = new TransactionResponse();

                transactionResponse.setId(transaction.getId());
                transactionResponse.setStatus(transaction.getStatus().name());
                transactionResponse.setUnit(transaction.getUnit().getUnitName().name());
                transactionResponse.setQuantity(transaction.getQuantity());
                transactionResponse.setPickupTime(
                    new SimpleDateFormat("HH:mm").format(transaction.getPickupTime()));

                BaseUserResponse userResponse = new BaseUserResponse();
                BeanUtils.copyProperties(transaction.getUser(), userResponse);

                transactionResponse.setUser(userResponse);

                OtherUserOfferResponse otherUserOfferResponse = new OtherUserOfferResponse();
                fillOfferResponse(otherUserOfferResponse, transaction);

                transactionResponse.setOffer(otherUserOfferResponse);

                List<Review> thisReview = reviewService.getReviewByTransaction(transaction);
                if(thisReview.size()==1 && thisReview.get(0).getUser().getId()==userPrincipal.getId()){
                    transactionResponse.setStatus(StatusEnum.REVIEWED.name());
                }

                myTransaction.add(transactionResponse);
            }
        }
        return myTransaction;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addTransaction(@CurrentUser UserPrincipal userPrincipal,
                                                      @RequestBody AddTransactionRequest request) {

        Offer offer = offerService.getOfferById(request.getOffer_id());
        boolean userCheck = userPrincipal.getId() != offer.getUser().getId();
        if (userCheck && offer != null && checkTakeTransaction(request.getQuantity(), offer)) {

            Transaction transaction = new Transaction();
            transaction.setUser(userService.getUserById(userPrincipal.getId()));
            transaction.setOffer(offer);
            transaction.setQuantity(request.getQuantity());
            transaction.setUnit(offer.getUnit());

            transaction.setStatus(StatusEnum.INIT);

            Date pickupTime = null;
            try {
                pickupTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                    .parse(request.getPickupTime());
            } catch (ParseException e) {
                logger.info("Failed when save transaction data, because: " + e);
            }
            if (pickupTime == null) {
                logger.info("pickup time null");
                return new ResponseEntity(
                    new ApiResponse(false, "Failed add transaction because pick up time not valid"),
                    HttpStatus.BAD_REQUEST);
            }
            transaction.setPickupTime(pickupTime);

            if (transactionService.saveTransaction(transaction) != null) {
                logger.info("Success when save transaction data");

                return new ResponseEntity(new ApiResponse(true, "Success add transaction"),
                    HttpStatus.OK);
            } else {
                logger.info("Failed when save transaction data");
            }

        }
        logger.info("Failed when save transaction data because checking, userCheck:" + userCheck
                    + "offer: " + offer.toString());
        return new ResponseEntity(new ApiResponse(false, "Failed add transaction"),
            HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/accepted")
    public ResponseEntity<ApiResponse> updateTransactionToAccepted(@CurrentUser UserPrincipal userPrincipal,
                                                                   @RequestBody UpdateTransactionStatusRequest request) {
        Transaction transaction = transactionService.getTransactionById(request.getId());
        Offer offer = offerService.getOfferById(transaction.getOffer().getId());
        if (transaction != null && transaction.getOffer().getUser().getId() == userPrincipal.getId()
            && transaction.getStatus() == StatusEnum.INIT
            && checkTakeTransaction(transaction.getQuantity(), offer)) {

            transaction.setStatus(StatusEnum.ACCEPTED);

            if (transactionService.saveTransaction(transaction) != null) {

                offer.setQuantity(offer.getQuantity() - transaction.getQuantity());

                if (offerService.saveOffer(offer) != null) {

                    return new ResponseEntity<>(new ApiResponse(true, "Success save transaction"),
                        HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<>(new ApiResponse(false, "Failed save transaction"),
            HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/rejected")
    public ResponseEntity<ApiResponse> updateTransactionToRejected(@CurrentUser UserPrincipal userPrincipal,
                                                                   @RequestBody UpdateTransactionStatusRequest request) {
        Transaction transaction = transactionService.getTransactionById(request.getId());
        if (transaction != null && transaction.getStatus() != StatusEnum.DONE
            && transaction.getStatus() != StatusEnum.EXPIRED
            && transaction.getOffer().getUser().getId() == userPrincipal.getId()) {

            Offer offer = transaction.getOffer();

            if (transaction.getStatus().equals(StatusEnum.ACCEPTED)) {
                offer.setQuantity(offer.getQuantity() + transaction.getQuantity());
            }

            transaction.setStatus(StatusEnum.REJECTED);

            if (transactionService.saveTransaction(transaction) != null
                && offerService.saveOffer(offer) != null) {

                return new ResponseEntity<>(new ApiResponse(true, "Success save transaction"),
                    HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new ApiResponse(false, "Failed save transaction"),
            HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/cancelled")
    public ResponseEntity<ApiResponse> updateTransactionToCancelled(@CurrentUser UserPrincipal userPrincipal,
                                                                   @RequestBody UpdateTransactionStatusRequest request) {
        Transaction transaction = transactionService.getTransactionById(request.getId());
        if (transaction != null && transaction.getStatus() == StatusEnum.INIT
                && transaction.getUser().getId() == userPrincipal.getId()) {

            transaction.setStatus(StatusEnum.REJECTED);

            if (transactionService.saveTransaction(transaction) != null) {

                return new ResponseEntity<>(new ApiResponse(true, "Success save transaction"),
                        HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new ApiResponse(false, "Failed save transaction"),
                HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/done")
    public ResponseEntity<ApiResponse> updateTranscationToDone(@CurrentUser UserPrincipal userPrincipal,
                                                               @RequestBody UpdateTransactionStatusRequest request) {
        Transaction transaction = transactionService.getTransactionById(request.getId());
        if (transaction != null && transaction.getStatus() != StatusEnum.DONE
            && transaction.getStatus() == StatusEnum.ACCEPTED
            && transaction.getOffer().getUser().getId() != userPrincipal.getId()) {

            transaction.setStatus(StatusEnum.DONE);

            if (transactionService.saveTransaction(transaction) != null) {
                return new ResponseEntity<>(new ApiResponse(true, "Success save transaction"),
                    HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new ApiResponse(false, "Failed save transaction"),
            HttpStatus.BAD_REQUEST);
    }

    private void fillOfferResponse(OtherUserOfferResponse otherUserOfferResponse,
                                   Transaction transaction) {

        BeanUtils.copyProperties(transaction.getOffer(), otherUserOfferResponse);
        otherUserOfferResponse.setUnit(transaction.getUnit().getUnitName().name());
        otherUserOfferResponse.setExpiryDate(
            new SimpleDateFormat("yyyy-MM-dd").format(transaction.getOffer().getExpiryDate()));

        List<String> imageUrls = new ArrayList<>();
        for (OfferImage offerImage : transaction.getOffer().getOfferImages()) {
            imageUrls.add(offerImage.getUrl());
        }
        otherUserOfferResponse.setImageUrls(imageUrls);

        BaseUserResponse baseUserResponse = new BaseUserResponse();
        BeanUtils.copyProperties(transaction.getOffer().getUser(), baseUserResponse);
        otherUserOfferResponse.setUser(baseUserResponse);

        otherUserOfferResponse.setLocation(transaction.getOffer().getUser().getCity());
    }

    private boolean checkTakeTransaction(Float quantity, Offer offer) {
        Date now = new Date();

        if (quantity != 0 && offer.getQuantity() - quantity >= 0
            && offer.getExpiryDate().after(now)) {
            return true;
        }
        logger.info("Failed when save transaction data when checking the take transaction, "
                    + quantity + "," + (offer.getQuantity() - quantity >= 0) + ","
                    + offer.getExpiryDate() + "-,-" + now+"timeCheck: "+offer.getExpiryDate().after(now));
        return false;
    }

}
