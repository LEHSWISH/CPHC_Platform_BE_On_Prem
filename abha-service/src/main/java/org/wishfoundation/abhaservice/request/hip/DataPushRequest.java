package org.wishfoundation.abhaservice.request.hip;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataPushRequest{
    public int pageNumber;
    public int pageCount;
    public String transactionId;
    public List<Entry> entries;
    public KeyMaterial keyMaterial;
}