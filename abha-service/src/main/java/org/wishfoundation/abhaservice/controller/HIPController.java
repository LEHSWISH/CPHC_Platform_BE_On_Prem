package org.wishfoundation.abhaservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.abhaservice.entity.CareContext;
import org.wishfoundation.abhaservice.request.hip.BaseHIPRequest;
import org.wishfoundation.abhaservice.request.hip.HIPConfirmRequest;
import org.wishfoundation.abhaservice.response.hip.BaseHIPModel;
import org.wishfoundation.abhaservice.service.HIPService;
import org.wishfoundation.abhaservice.utils.Helper;
import java.util.List;
/**
 * Controller for handling HIP (Health Information Portability) related requests.
 *
 * @author YourName
 * @version 1.0
 * @since 2023-01-01
 */
@RestController
@RequestMapping("/api/v1/abha-hip")
public class HIPController {

    /**
     * Autowired instance of HIPService.
     */
    @Autowired
    private HIPService hipService;

    /**
     * Initializes the HIP authentication process.
     *
     * @param request The initial HIP request.
     * @return ResponseEntity containing the result of the HIP initialization.
     */
    @PostMapping("/auth/init")
    public ResponseEntity<BaseHIPModel> init(@RequestBody BaseHIPRequest request) {
        try {
            System.out.println("BaseHIPRequest : "+ Helper.MAPPER.writeValueAsString(request));
            return hipService.init(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Confirms the HIP authentication process.
     *
     * @param request The HIP confirmation request.
     * @return ResponseEntity containing the result of the HIP confirmation.
     */
    @PostMapping("/auth/confirm")
    public ResponseEntity<BaseHIPModel> confirm(@RequestBody HIPConfirmRequest request) {
        try {
            return hipService.confim(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adds care contexts to the HIP link.
     *
     * @param request The HIP request containing the care contexts.
     * @return ResponseEntity containing the result of adding care contexts.
     */
    @PostMapping("/link/add-contexts")
    public ResponseEntity<BaseHIPModel> addContext(@RequestBody BaseHIPRequest request) {
        try {
            return hipService.addCareContext(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Notifies the user via SMS.
     *
     * @param request The HIP request containing the notification details.
     * @return ResponseEntity containing the result of the notification.
     */
    @PostMapping("/patients/sms/notify")
    public ResponseEntity<BaseHIPModel> notifyUser(@RequestBody BaseHIPRequest request) {
        try {
            System.out.println(" onDiscover : "+Helper.MAPPER.writeValueAsString(request));
            return hipService.notifyUser(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Handles the on-discover event for care contexts.
     *
     * @param request The HIP request containing the event details.
     * @return ResponseEntity containing the result of the on-discover event.
     */
    @PostMapping("/care-contexts/on-discover")
    public ResponseEntity<BaseHIPModel> onDiscover(@RequestBody BaseHIPRequest request) {
        try {
            System.out.println(" onDiscover : "+Helper.MAPPER.writeValueAsString(request));
            return hipService.onDiscover(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Handles the on-init event for HIP links.
     *
     * @param request The HIP request containing the event details.
     * @return ResponseEntity containing the result of the on-init event.
     */
    @PostMapping("/links/link/on-init")
    public ResponseEntity<BaseHIPModel> linkOnInit(@RequestBody BaseHIPRequest request) {
        try {
            return hipService.linkOnInit(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Handles the on-confirm event for HIP links.
     *
     * @param request The HIP request containing the event details.
     * @return ResponseEntity containing the result of the on-confirm event.
     */
    @PostMapping("/links/link/on-confirm")
    public ResponseEntity<BaseHIPModel> onConfirm(@RequestBody BaseHIPRequest request) {
        try {
            return hipService.onConfirm(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // FOR PORTAL

    @PostMapping("/get-unlinked-care-context")
    public ResponseEntity<List<CareContext>> getCareContext(@RequestBody BaseHIPRequest request) {
        try {
            return hipService.getCareContext(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
