package org.wishfoundation.userservice.controller;

import org.springframework.web.bind.annotation.*;
import org.wishfoundation.userservice.response.TourismUserDetails;
import org.wishfoundation.userservice.service.TourismPortalServiceImpl;

/**
 * This class is responsible for handling requests related to the Tourism Portal.
 * It uses the TourismPortalServiceImpl to perform operations.
 */
@RestController
@RequestMapping("/api/v1/tourism")
public class TourismPortalController {

    /**
     * Instance of TourismPortalServiceImpl for performing operations.
     */
    private final TourismPortalServiceImpl tourismPortalService;

    /**
     * Constructor for TourismPortalController.
     *
     * @param tourismPortalService instance of TourismPortalServiceImpl
     */
    public TourismPortalController(TourismPortalServiceImpl tourismPortalService){
        this.tourismPortalService = tourismPortalService;
    };


    /**
     * This method retrieves user details from the Tourism Portal based on the provided ID.
     *
     * @param id       Tourism Portal ID a unique identifier of the user
     * @param consent  indicates whether the user has given consent for data retrieval
     * @return TourismUserDetails object containing user details
     */
    @RequestMapping(method = RequestMethod.GET,path = "/getUserInfoByIDTP/{id}")
    public TourismUserDetails getUserByIDTP(@PathVariable("id") String id ,
                                            @RequestParam(value = "consent", defaultValue = "false") boolean consent){
        //consent is passed as a parameter as a boolean and is false by default
        return tourismPortalService.getUserByIDTP(id,consent);
    }
}
