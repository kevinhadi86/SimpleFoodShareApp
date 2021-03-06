/**
 * Alipay.com Inc.
 * Copyright (c) 2004‐2019 All Rights Reserved.
 */
package edible.simple.payload.review;

import edible.simple.payload.transcation.TransactionResponse;
import edible.simple.payload.user.BaseUserResponse;

/**
 * @author Kevin Hadinata
 * @version $Id: ReviewResponse.java, v 0.1 2019‐09‐23 00:41 Kevin Hadinata Exp $$
 */
public class ReviewResponse {

    private Long id;
    private TransactionResponse transaction;
    private Float rating;
    private String review;
    private String date;
    private BaseUserResponse user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionResponse getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionResponse transaction) {
        this.transaction = transaction;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BaseUserResponse getUser() {
        return user;
    }

    public void setUser(BaseUserResponse user) {
        this.user = user;
    }
}
