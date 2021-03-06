/**
 * Alipay.com Inc.
 * Copyright (c) 2004‐2019 All Rights Reserved.
 */
package edible.simple.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edible.simple.model.Review;
import edible.simple.model.Transaction;
import edible.simple.model.User;
import edible.simple.repository.ReviewRepository;

/**
 * @author Kevin Hadinata
 * @version $Id: ReviewServiceImpl.java, v 0.1 2019‐09‐23 00:35 Kevin Hadinata Exp $$
 */
@Service
public class ReviewServiceImpl implements ReviewService{

    @Autowired
    ReviewRepository reviewRepository;

    @Override
    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getMyReview(User user) {
        List<Review> myReview = new ArrayList<>();

        List<Review> myReviewAsTransactionUser = reviewRepository.findAllByTransaction_User(user);
        if(myReviewAsTransactionUser.size() != 0){
            for (Review reviewAsTransactionUser: myReviewAsTransactionUser) {
                if(reviewAsTransactionUser.getUser().getId() != user.getId()){
                    myReview.add(reviewAsTransactionUser);
                }
            }
        }

        List<Review> myReviewAsOfferUser = reviewRepository.findAllByTransaction_Offer_User(user);
        if(myReviewAsOfferUser.size() != 0){
            for (Review reviewAsOfferUser: myReviewAsOfferUser) {
                if(reviewAsOfferUser.getUser().getId() != user.getId()){
                    myReview.add(reviewAsOfferUser);
                }
            }
        }

        return myReview;
    }

    @Override
    public List<Review> getReviewByUser(User user) {
        return reviewRepository.findAllByTransaction_User(user);
    }

    @Override
    public List<Review> getReviewByOwner(User user) {
        return reviewRepository.findAllByTransaction_Offer_User(user);
    }

    @Override
    public List<Review> getReviewByTransaction(Transaction transaction) {
        return reviewRepository.findByTransaction(transaction);
    }
}
