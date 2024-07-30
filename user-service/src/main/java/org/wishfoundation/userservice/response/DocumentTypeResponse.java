package org.wishfoundation.userservice.response;

import java.util.List;

import org.wishfoundation.userservice.enums.GovernmentIdType;

import lombok.Data;

@Data
public class DocumentTypeResponse {
	List<GovernmentIdType> documentType;
}
