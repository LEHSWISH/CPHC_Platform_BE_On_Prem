package org.wishfoundation.abhaservice.request.hip;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Entry {
    public String content;
    public String media;
    public String checksum;
    public String careContextReference;
}
