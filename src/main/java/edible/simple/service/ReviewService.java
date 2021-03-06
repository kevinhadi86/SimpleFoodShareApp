/**
 * Alipay.com Inc.
 * Copyright (c) 2004‐2019 All Rights Reserved.
 */
package edible.simple.service;

import java.util.List;

import edible.simple.model.Review;
import edible.simple.model.Transaction;
import edible.simple.model.User;

/**
 * @author Kevin Hadinata
 * @version $Id: ReviewService.java, v 0.1 2019‐09‐23 00:29 Kevin Hadinata Exp $$
 */
public interface ReviewService {

    public Review addReview(Review review);

    public List<Review> getMyReview(User user);

    public List<Review> getReviewByUser(User user);

    public List<Review> getReviewByOwner(User user);

    public List<Review> getReviewByTransaction(Transaction transaction);

}