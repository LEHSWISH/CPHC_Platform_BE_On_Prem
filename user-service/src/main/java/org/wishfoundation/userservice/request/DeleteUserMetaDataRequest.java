package org.wishfoundation.userservice.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteUserMetaDataRequest {
    boolean abhaDetails;
    boolean yatriDetails;
    boolean tourismDetails;
}
