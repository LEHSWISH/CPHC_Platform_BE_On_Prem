package org.wishfoundation.userservice.service;

import org.springframework.http.ResponseEntity;
import org.wishfoundation.userservice.request.*;
import org.wishfoundation.userservice.response.*;

import java.util.List;

/**
 * This interface defines the contract for YatriPulseUserService.
 * It provides methods for managing Yatri users and their associated data.
 */
public interface YatriPulseUserService {

    /**
     * Updates the Yatri user's information.
     *
     * @param yatriPulseUserRequest The updated user information.
     * @param userName The unique identifier of the Yatri user.
     * @return ResponseEntity with HTTP status 200 (OK) if the update is successful, otherwise an appropriate error response.
     */
    public ResponseEntity<Void> updateYatriUser(YatriPulseUserRequest yatriPulseUserRequest, String userName);

    /**
     * Deletes the Yatri user from the system.
     *
     * @param userName The unique identifier of the Yatri user.
     */
    public void deleteYatri(String userName);

    /**
     * Retrieves the Yatri user's information.
     *
     * @param userName The unique identifier of the Yatri user.
     * @param phoneNumber The phone number of the Yatri user.
     * @return The Yatri user's information if found, otherwise null.
     */
    public YatriPulseUserResponse getYatriPulseUser(String userName, String phoneNumber);

    /**
     * Sends an OTP (One Time Password) to the user's registered phone number.
     *
     * @param otpRequest The request containing the user's phone number.
     * @return The OTP response containing the generated OTP and other relevant information.
     */
    public OtpResponse sendOTP(OtpRequest otpRequest);

    /**
     * Verifies the received OTP with the stored value.
     *
     * @param otpRequest The request containing the received OTP and the user's phone number.
     * @return The OTP response indicating whether the verification was successful or not.
     */
    public OtpResponse verifyOTP(OtpRequest otpRequest);

    /**
     * Resends an OTP to the user's registered phone number.
     *
     * @param otpRequest The request containing the user's phone number.
     * @return The OTP response containing the newly generated OTP and other relevant information.
     */
    public OtpResponse resendOTP(OtpRequest otpRequest);

    /**
     * Retrieves the list of available document types for Yatri users.
     *
     * @return The response containing the list of available document types.
     */
    public DocumentTypeResponse documentType();

    /**
     * Retrieves the medical reports associated with the Yatri user.
     *
     * @return The response containing the medical reports.
     */
    public MedicalsReportsResponse getMedicalReports();

    /**
     * Deletes the specified medical documents associated with the Yatri user.
     *
     * @param documentsPathRequestList The list of document paths to be deleted.
     * @return ResponseEntity with HTTP status 200 (OK) if the deletion is successful, otherwise an appropriate error response.
     */
    public ResponseEntity<Void> deleteMedicalDocument(List<DocumentsPathRequest> documentsPathRequestList);

    /**
     * Retrieves the vitals record of the Yatri user.
     *
     * @return The response containing the vitals record.
     */
    public VitalsRecordResponse fetchVitalsRecord();
}
