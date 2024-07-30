package org.wishfoundation.userservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.ABHAUserDetails;
import org.wishfoundation.userservice.entity.ActorEntity;
import org.wishfoundation.userservice.entity.YatriDetails;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.entity.repository.ABHAUserDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriDetailsRepository;
import org.wishfoundation.userservice.entity.repository.YatriPulseUsersRepository;
import org.wishfoundation.userservice.request.abha.BaseDiscoveryRequest;
import org.wishfoundation.userservice.response.YatriPulseUserResponse;
import org.wishfoundation.userservice.service.YatriPulseUserServiceImpl;
import org.wishfoundation.userservice.utils.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/abha-detail")
public class AbhaDetailController {

    @Autowired
    private ABHAUserDetailsRepository abhaUserDetailsRepository;

    @Autowired
    private YatriPulseUsersRepository yatrisRepo;

    @Autowired
    private YatriDetailsRepository yatriDetailsRepo;

    @Autowired
    private  YatriPulseUserServiceImpl yatriPulseUserService;

    @PostMapping
    public ABHAUserDetails getAbhaDb(){

        Optional<YatriPulseUsers> byId = yatrisRepo.findById(UserContext.getUserId());
        if(byId.isPresent()){
            YatriPulseUsers yatriPulseUsers = byId.get();
            UUID abhaUserId = yatriPulseUsers.getAbhaUserId();
            if(!ObjectUtils.isEmpty(abhaUserId)){
                ABHAUserDetails abhaUserDetails = abhaUserDetailsRepository.findById(abhaUserId).get();
                abhaUserDetails.setAbhaNumber(Helper.decrypt(abhaUserDetails.getAbhaNumber()));
                abhaUserDetails.setPhrAddress(Helper.decryptList(abhaUserDetails.getPhrAddress()));
                return abhaUserDetails;
            }
        }
        return null;
    }

    @PostMapping(path = "/abha-id")
    public ABHAUserDetails getAbhaDb(@RequestBody ABHAUserDetails request){
        if(StringUtils.hasLength(request.getAbhaNumber())) {
            Optional<ABHAUserDetails> byAbhaNumber = abhaUserDetailsRepository.findByAbhaNumber(Helper.encrypt(request.getAbhaNumber()));
            if (byAbhaNumber.isPresent()) {
                ABHAUserDetails abhaUserDetails = byAbhaNumber.get();
                abhaUserDetails.setAbhaNumber(Helper.decrypt(abhaUserDetails.getAbhaNumber()));
                abhaUserDetails.setPhrAddress(Helper.decryptList(abhaUserDetails.getPhrAddress()));
                return abhaUserDetails;
            }
        }
        return new ABHAUserDetails();
    }

    @PostMapping(path = "/user-detail")
    public YatriPulseUserResponse getAbhaDb(@RequestBody BaseDiscoveryRequest request){

        List<String> usersByPhoneNumber = yatrisRepo.findUsersByPhoneNumber(request.getPhoneNumber());

        if(!ObjectUtils.isEmpty(usersByPhoneNumber)) {
            ArrayList<YatriDetails> users = yatriDetailsRepo.findByCreatedByUserNames(usersByPhoneNumber);
            UUID yatriId = null;
            YatriDetails yatriDetails = null;


            for( YatriDetails yd :  users){
                if(ObjectUtils.isEmpty(yd.getGender()) || StringUtils.isEmpty(yd.getDateOfBirth()))
                    continue;

                String gender = String.valueOf(yd.getGender().name().charAt(0)) ;
                int dobYear = Integer.valueOf(yd.getDateOfBirth().split("/")[2]);
                int diff = Math.abs(dobYear - request.getYearOfBirth());
                String fname = request.getName().split(" ")[0];
                if (gender.equalsIgnoreCase(request.getGender()) && (dobYear == request.getYearOfBirth() || diff <= 2) && yd.getFullName().contains(fname)) {
                    yatriId = yd.getYatriPulseUserId();
                    yatriDetails = yd;
                }
            }

            if (!ObjectUtils.isEmpty(yatriId)){
                ActorEntity createdBy = yatriDetails.getCreatedBy();
                YatriPulseUserResponse yatriPulseUser = yatriPulseUserService.getYatriPulseUser(createdBy.getAuditUserName(), createdBy.getAuditPhoneNumber());
                return  yatriPulseUser;
            }
        }
        return null;
    }
}