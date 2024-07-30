package org.wishfoundation.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.userservice.config.UserContext;
import org.wishfoundation.userservice.entity.YatriPulseUsers;
import org.wishfoundation.userservice.entity.repository.*;
import org.wishfoundation.userservice.request.DeleteUserMetaDataRequest;
import org.wishfoundation.userservice.utils.Helper;

import java.util.UUID;

//TODO : delete this class after testing
@RestController
@RequestMapping("/delete/")
public class DeleteUserMetaDataController {
    @Autowired
    private YatriPulseUsersRepository yatriPulseUsersRepo;

    @Autowired
    private YatriDetailsRepository yatriDetailsRepo;

    @Autowired
    private TourismUserInfoRepository tourismUserInfoRepository;

    @Autowired
    private ABHAUserDetailsRepository abhaUserDetailsRepo;

    @DeleteMapping("user-metadata")
    public ResponseEntity<Void> deleteUserMetaData(@RequestBody DeleteUserMetaDataRequest deleteUserMetaDataRequest) {
        YatriPulseUsers yatriPulseUsers = yatriPulseUsersRepo.findByUserNameAndPhoneNumber(UserContext.getCurrentUserName(),UserContext.getCurrentPhoneNumber());
       if (deleteUserMetaDataRequest.isAbhaDetails()){
           UUID id = yatriPulseUsers.getAbhaUserId();
           yatriPulseUsers.setAbhaUserId(null);
           yatriPulseUsersRepo.save(yatriPulseUsers);

           abhaUserDetailsRepo.deleteById(id);
       }
        if (deleteUserMetaDataRequest.isYatriDetails()){
            UUID id = yatriPulseUsers.getYatriDetailsId();
            yatriPulseUsers.setYatriDetailsId(null);
            yatriPulseUsersRepo.save(yatriPulseUsers);

            yatriDetailsRepo.deleteById(id);
        }

        if (deleteUserMetaDataRequest.isTourismDetails()){
            UUID id = yatriPulseUsers.getTourismId();
            yatriPulseUsers.setTourismId(null);
            yatriPulseUsersRepo.save(yatriPulseUsers);

            tourismUserInfoRepository.deleteById(id);
        }


        return new ResponseEntity<>(HttpStatus.OK);
    }
}
