package org.wishfoundation.abhaservice.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.wishfoundation.abhaservice.enums.FieldType;
import org.wishfoundation.abhaservice.exception.WishFoundationException;
import org.wishfoundation.chardhamcore.enums.ErrorCode;
import org.wishfoundation.chardhamcore.security.JWTService;

@Component
public class AbhaApiUtils {

    @Autowired
    private JWTService jwtService;

    public void checkValidation(String fieldValue, FieldType fieldType) throws WishFoundationException {
        boolean isValid = false;
        if (ObjectUtils.isEmpty(fieldValue)) {
            throwExceptionForDocumentType(fieldType);
        }

        switch (fieldType) {
            case PASSPORT:
                isValid = fieldValue.matches("^[A-PR-WY-Z][1-9]\\d\\d{4}[1-9]$");
                break;
            case PAN_CARD:
                isValid = fieldValue.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
                break;
            case AADHAR_CARD:
                isValid = fieldValue.matches("^[2-9]\\d{3}\\d{4}\\d{4}$");
                break;
            case VOTER_ID_CARD:
                isValid = fieldValue.matches("^[A-Z]{3}\\d{7}$") || fieldValue.matches("^[A-Z]{3}[0-9]{7}$");
                break;
            case DRIVING_LICENSE:
                isValid = fieldValue.matches("^(([A-Z]{2}[0-9]{2})|([A-Z]{2}-[0-9]{2}))((19|20)[0-9][0-9])[0-9]{7}$");
                break;
            case OTP:
                isValid = fieldValue.matches("^[0-9]{6}$");
                break;
            case MOBILE:
                isValid = fieldValue.matches("^[0-9]{10}$");
                break;
            case CONSENT:
                isValid = Boolean.valueOf(fieldValue);
                break;
            case ABHA_ADDRESS:
                isValid = true;
                break;
            default:
                break;
        }
        if (!isValid) {
            throwExceptionForDocumentType(fieldType);
        }
    }

    private void throwExceptionForDocumentType(FieldType governmentIdType) throws WishFoundationException {
        switch (governmentIdType) {
            case PASSPORT:
                throw new WishFoundationException(ErrorCode.INVALID_PASSPORT.getCode(), ErrorCode.INVALID_PASSPORT.getMessage());
            case PAN_CARD:
                throw new WishFoundationException(ErrorCode.INVALID_PAN_CARD.getCode(), ErrorCode.INVALID_PAN_CARD.getMessage());
            case AADHAR_CARD:
                throw new WishFoundationException(ErrorCode.INVALID_AADHAAR_NUMBER.getCode(), ErrorCode.INVALID_AADHAAR_NUMBER.getMessage());
            case VOTER_ID_CARD:
                throw new WishFoundationException(ErrorCode.INVALID_VOTER_ID.getCode(), ErrorCode.INVALID_VOTER_ID.getMessage());
            case DRIVING_LICENSE:
                throw new WishFoundationException(ErrorCode.INVALID_DRIVING_LICENSE.getCode(), ErrorCode.INVALID_DRIVING_LICENSE.getMessage());
            case OTP:
                throw new WishFoundationException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
            case MOBILE:
                throw new WishFoundationException(ErrorCode.INVALID_PHONE_NUMBER.getCode(), ErrorCode.INVALID_PHONE_NUMBER.getMessage());
            case TXT_ID:
                throw new WishFoundationException(ErrorCode.INVALID_TRANSACTION_ID.getCode(), ErrorCode.INVALID_TRANSACTION_ID.getMessage());
            case CONSENT:
                throw new WishFoundationException(ErrorCode.UNCONSENT_REQUEST.getCode(), ErrorCode.UNCONSENT_REQUEST.getMessage());
            case ABHA_ADDRESS:
                throw new WishFoundationException("400", "ABHA Address not present", HttpStatus.BAD_REQUEST);
            case OTP_RATE_LIMIT:
                throw new WishFoundationException(ErrorCode.OTP_RATE_LIMIT.getCode(), ErrorCode.OTP_RATE_LIMIT.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
            default:
                throw new WishFoundationException(ErrorCode.INVALID_GOVERNMENT_ID.getCode(), ErrorCode.INVALID_GOVERNMENT_ID.getMessage());
        }
    }

}
