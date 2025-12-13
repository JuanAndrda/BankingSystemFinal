package com.banking.models;

import com.banking.utilities.ValidationPatterns;

/**
 * Extended customer information and contact details.
 * Demonstrates 1-to-1 relationship with Customer.
 * All contact fields are validated for correctness and completeness.
 */
public class CustomerProfile {
    private String profileId;      // Format: P###
    private String address;        // Customer's mailing address (non-empty)
    private String phone;          // Phone number (minimum 10 digits)
    private String email;          // Email address (valid format)
    private Customer customer;     // 1-to-1: Reference to associated customer


    public CustomerProfile(String profileId, String address, String phone, String email) {
        this.setProfileId(profileId);
        this.setAddress(address);
        this.setPhone(phone);
        this.setEmail(email);
    }

    // ===== GETTERS =====
    /** @return profile ID (format P###) */
    public String getProfileId() {
        return this.profileId;
    }

    /** @return customer's mailing address */
    public String getAddress() {
        return this.address;
    }

    /** @return customer's phone number */
    public String getPhone() {
        return this.phone;
    }

    /** @return customer's email address */
    public String getEmail() {
    return this.email;
    }

    /** @return associated customer (1-to-1 relationship) */
    public Customer getCustomer() {
        return this.customer;
    }

    // ===== SETTERS WITH VALIDATION =====
    public void setProfileId(String profileId) {
        if (ValidationPatterns.matchesPattern(profileId, ValidationPatterns.PROFILE_ID_PATTERN)) {
            throw new IllegalArgumentException(ValidationPatterns.PROFILE_ID_ERROR);
        }
        this.profileId = profileId;
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException(ValidationPatterns.ADDRESS_EMPTY_ERROR);
        }
        this.address = address.trim();
    }

    public void setPhone(String phone) {
        if (!ValidationPatterns.isValidPhoneNumber(phone)) {
            throw new IllegalArgumentException(ValidationPatterns.PHONE_ERROR);
        }
        this.phone = phone;
    }

    public void setEmail(String email) {
        if (!ValidationPatterns.matchesPattern(email, ValidationPatterns.EMAIL_PATTERN)) {
            throw new IllegalArgumentException(ValidationPatterns.EMAIL_ERROR);
        }
        this.email = email;
    }

    public void setCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException(ValidationPatterns.CUSTOMER_NULL_ERROR);
        }
        this.customer = customer;
    }

    @Override
    public String toString() {
        return String.format("Profile[ID=%s, Address=%s, Phone=%s, Email=%s]",
                this.getProfileId(), this.getAddress(), this.getPhone(), this.getEmail());
    }
}
