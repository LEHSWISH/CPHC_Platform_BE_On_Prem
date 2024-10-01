package org.wishfoundation.userservice.controller;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.enums.CreationType;
import org.wishfoundation.userservice.request.*;
import org.wishfoundation.userservice.response.*;
import org.wishfoundation.userservice.service.ForgetUserPasswordServiceImpl;
import org.wishfoundation.userservice.service.YatriPulseUserLoginSignUpServiceImpl;
import org.wishfoundation.userservice.service.YatriPulseUserServiceImpl;
import org.wishfoundation.userservice.utils.Helper;

/**
 * This is the controller class for handling all the API requests related to YatriPulseUser.
 * It provides endpoints for user registration, login, password reset, user details update,
 * OTP handling, document management, and other related functionalities.
 */
@RestController
@RequestMapping("/api/v1/yatri")
@AllArgsConstructor
public class YatriPulseUserController {

  @Autowired
  private final YatriPulseUserServiceImpl yatriPulseUserService;

  @Autowired
  private final YatriPulseUserLoginSignUpServiceImpl yatriPulseUserLoginSignUp;

  @Autowired
  private final ForgetUserPasswordServiceImpl forgetUserPasswordService;

  @Autowired
  private YatriPulseUsersRepository yatriPulseUsersRepository;

  /**
   * This method is used to register a new YatriPulseUser.
   *
   * @param yatriPulseUserRequest The request object containing the user details.
   * @return ResponseEntity with HTTP status 200 (OK) if the user is successfully registered.
   * ResponseEntity with HTTP status 400 (Bad Request) if the request is invalid or the user already exists.
   * ResponseEntity with HTTP status 500 (Internal Server Error) if there is an error during the registration process.
   */
  @PostMapping("/sign-up")
  public ResponseEntity<Void> signUp(
    @Valid @RequestBody YatriPulseUserRequest yatriPulseUserRequest
  ) {
    return yatriPulseUserLoginSignUp.registerYatri(yatriPulseUserRequest);
  }

  /**
   * This method is used to register multiple users at once from an excel file.
   *
   * @param yatriPulseUserRequestList List of {@link ExcelUserCreationRequest} containing user details.
   * @return ResponseEntity with HashMap containing success and failure counts.
   */
  @PostMapping("/excel-sign-up")
  public ResponseEntity<HashMap> excelSignUp(
    @Valid @RequestBody ArrayList<ExcelUserCreationRequest> yatriPulseUserRequestList
  ) {
    return yatriPulseUserLoginSignUp.excelRegisterYatri(
      yatriPulseUserRequestList
    );
  }

  /**
   * This method is used to split an excel file into chunks of Json for batch processing.
   *
   * @param yatriPulseUserRequestFile MultipartFile containing the excel file.
   * @return ResponseEntity with StreamingResponseBody containing the split excel data.
   * @throws IOException If there is an error reading the file.
   */
  @PostMapping("/split-file")
  public ResponseEntity<StreamingResponseBody> splitFile(
    @Valid @RequestParam("file") MultipartFile yatriPulseUserRequestFile
  ) throws IOException {
    if (Helper.validateFileType(yatriPulseUserRequestFile)) {
      return Helper.convertExceltoList(
        yatriPulseUserRequestFile.getInputStream()
      );
    }
    return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(responseBody -> {
        responseBody.write("Invalid file type".getBytes());
      });
  }

  /**
   * Validates the existence of a user by their username.
   *
   * @param userName The username of the user to validate.
   * @return ResponseEntity with HTTP status 200 (OK) if the user exists.
   * ResponseEntity with HTTP status 404 (Not Found) if the user does not exist.
   */
  @GetMapping("/validate-user/{user-name}")
  public ResponseEntity<Void> validateUser(
    @PathVariable("user-name") String userName
  ) {
    return yatriPulseUserLoginSignUp.validateUser(userName);
  }

  /**
   * Handles user login.
   *
   * @param yatriPulseUserRequest The request object containing the user's login credentials.
   * @return ResponseEntity with HTTP status 200 (OK) and LoginResponse object if the login is successful.
   * ResponseEntity with HTTP status 401 (Unauthorized) if the login credentials are invalid.
   */
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
    @RequestBody YatriPulseUserRequest yatriPulseUserRequest
  ) {
    return yatriPulseUserLoginSignUp.login(yatriPulseUserRequest);
  }

  /**
   * Resets a user's password.
   *
   * @param yatriPulseUserRequest The request object containing the user's username and new password.
   * @return PasswordResetResponse object containing the status of the password reset operation.
   */
  @PostMapping("/reset-password")
  public PasswordResetResponse resetPassword(
    @RequestBody YatriPulseUserRequest yatriPulseUserRequest
  ) {
    return forgetUserPasswordService.resetPassword(yatriPulseUserRequest);
  }

  /**
   * Updates the details of a YatriPulseUser.
   *
   * @param yatriPulseUserRequest The request object containing the updated user details.
   * @return ResponseEntity with HTTP status 200 (OK) if the user is successfully updated.
   * ResponseEntity with HTTP status 400 (Bad Request) if the request is invalid.
   */
  @PostMapping("/update")
  public ResponseEntity<Void> updateYatri(
    @Valid @RequestBody YatriPulseUserRequest yatriPulseUserRequest
  ) {
    return yatriPulseUserService.updateYatriUser(
      yatriPulseUserRequest,
      UserContext.getCurrentUserName()
    );
  }

  /**
   * Deletes a YatriPulseUser.
   *
   * @param id The ID of the user to delete.
   */
  @PostMapping("/remove-user/{id}")
  public void deleteYatri(@PathVariable("id") String id) {
    yatriPulseUserService.deleteYatri(id);
  }

  /**
   * Fetches the details of the currently logged-in YatriPulseUser.
   *
   * @return YatriPulseUserResponse object containing the user's details.
   */
  @GetMapping("/get-user")
  public YatriPulseUserResponse getYatriPulseUser() {
    return yatriPulseUserService.getYatriPulseUser(
      UserContext.getCurrentUserName(),
      UserContext.getCurrentPhoneNumber()
    );
  }

  /**
   * This method is used to fetch the list of bulk users who were created from an excel file.
   *
   * @return List of {@link BulkUserDetailsResponse} containing the details of bulk users.
   */
  @GetMapping("/get-user/Bulk-User")
  public List<BulkUserDetailsResponse> fetchBulkUsers() {
    return yatriPulseUsersRepository.findUserDetailsbyCreationType(
      CreationType.TOURISM_EXCEL
    );
  }

  /**
   * Sends an OTP to the user's registered phone number.
   *
   * @param otpRequest The request object containing the user's phone number.
   * @return OtpResponse object containing the status of the OTP sending process.
   */
  @PostMapping("/send-otp")
  public OtpResponse sendOTP(@Valid @RequestBody OtpRequest otpRequest) {
    return yatriPulseUserService.sendOTP(otpRequest);
  }

  /**
   * Verifies the received OTP.
   *
   * @param otpRequest The request object containing the user's phone number and the received OTP.
   * @return OtpResponse object containing the status of the OTP verification process.
   */
  @PostMapping("/verify-otp")
  public OtpResponse verifyOTP(@Valid @RequestBody OtpRequest otpRequest) {
    return yatriPulseUserService.verifyOTP(otpRequest);
  }

  /**
   * Resends the OTP to the user's registered phone number.
   *
   * @param otpRequest The request object containing the user's phone number.
   * @return OtpResponse object containing the status of the OTP resending process.
   */
  @PostMapping("/resend-otp")
  public OtpResponse resendOTP(@Valid @RequestBody OtpRequest otpRequest) {
    return yatriPulseUserService.resendOTP(otpRequest);
  }

  /**
   * Forgets the username of a user.
   *
   * @param forgetUserName The request object containing the user's phone number.
   * @return ResponseEntity with HTTP status 200 (OK) if the username is successfully sent to the user's registered phone number.
   * ResponseEntity with HTTP status 400 (Bad Request) if the request is invalid.
   */
  @PostMapping("/forget-username")
  public ResponseEntity<Void> forgetUsername(
    @Valid @RequestBody ForgetUserName forgetUserName
  ) {
    return forgetUserPasswordService.forgetUsername(forgetUserName);
  }

  /**
   * Checks if a phone number is linked to any user.
   *
   * @param phoneNumber The phone number to check.
   * @return PhoneNumberLinkedResponse object containing the status of the phone number linking.
   */
  @GetMapping("/phone-number/linked/{phone-number}")
  public PhoneNumberLinkedResponse phoneNumberLinked(
    @PathVariable("phone-number") String phoneNumber
  ) {
    return yatriPulseUserLoginSignUp.phoneNumberLinked(phoneNumber, false);
  }

  /**
   * Fetches the list of document types that can be uploaded by users.
   *
   * @return DocumentTypeResponse object containing the list of document types.
   */
  @GetMapping("/fetch/document-type")
  public DocumentTypeResponse documentType() {
    return yatriPulseUserService.documentType();
  }

  /**
   * Fetches the list of medical reports uploaded by users.
   *
   * @return MedicalsReportsResponse object containing the list of medical reports.
   */
  @GetMapping("/fetch/medical-reports")
  public MedicalsReportsResponse getMedicalReports() {
    return yatriPulseUserService.getMedicalReports();
  }

  /**
   * Deletes medical documents uploaded by users.
   *
   * @param documentsPathRequestList The list of request objects containing the paths of the documents to delete.
   * @return ResponseEntity with HTTP status 200 (OK) if the documents are successfully deleted.
   * ResponseEntity with HTTP status 400 (Bad Request) if the request is invalid.
   */
  @PostMapping("/delete-medical-document")
  public ResponseEntity<Void> deleteMedicalDocument(
    @RequestBody List<DocumentsPathRequest> documentsPathRequestList
  ) {
    return yatriPulseUserService.deleteMedicalDocument(
      documentsPathRequestList
    );
  }

  /**
   * Validates the combination of username and phone number.
   *
   * @param validatePhoneNumberUserName The request object containing the username and phone number to validate.
   * @return ResponseEntity with HTTP status 200 (OK) if the combination is valid.
   * ResponseEntity with HTTP status 400 (Bad Request) if the combination is invalid.
   */
  @PostMapping("/validate/user-name-phone-number")
  public ResponseEntity<Void> validateUserNamePhoneNumber(
    @RequestBody ValidatePhoneNumberUserName validatePhoneNumberUserName
  ) {
    return forgetUserPasswordService.validateUserNamePhoneNumber(
      validatePhoneNumberUserName
    );
  }

  /**
   * Fetches the list of users linked to a phone number.
   *
   * @param phoneNumber The phone number to check.
   * @return PhoneNumberLinkedResponse object containing the list of users linked to the phone number.
   */
  @GetMapping("/phone-number/linked/users/{phone-number}")
  public PhoneNumberLinkedResponse phoneNumberLinkedUsers(
    @PathVariable("phone-number") String phoneNumber
  ) {
    return yatriPulseUserLoginSignUp.phoneNumberLinked(phoneNumber, true);
  }

  /**
   * Fetches the vitals record of a user.
   *
   * @return VitalsRecordResponse object containing the vitals record.
   */
  @GetMapping("/fetch-vitals")
  public VitalsRecordResponse fetchVitalsRecord() {
    return yatriPulseUserService.fetchVitalsRecord();
  }

  /**
   * Checks if a user with a given username exists.
   *
   * @param userName The username to check.
   * @return ResponseEntity with HTTP status 200 (OK) if the user exists.
   * ResponseEntity with HTTP status 404 (Not Found) if the user does not exist.
   */
  @GetMapping("/check-user-existence/{user-name}")
  public ResponseEntity<Void> checkUserExistence(
    @PathVariable("user-name") String userName
  ) {
    return yatriPulseUserLoginSignUp.checkUserExistence(userName);
  }
}
