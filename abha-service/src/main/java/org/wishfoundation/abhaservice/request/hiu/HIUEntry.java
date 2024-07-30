package org.wishfoundation.abhaservice.request.hiu;

import lombok.Data;

@Data
public class HIUEntry {

    public String content;
    public String media;
    public String checksum;
    public String careContextReference;
}
