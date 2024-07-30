package org.wishfoundation.abhaservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wishfoundation.abhaservice.request.ABHAFlowChainRequest;
import org.wishfoundation.abhaservice.request.hip.BaseHIPRequest;
import org.wishfoundation.abhaservice.request.hip.HIPConfirmRequest;
import org.wishfoundation.abhaservice.request.hip.Link;
import org.wishfoundation.abhaservice.request.webhook.BaseHIPWebhookRequest;
import org.wishfoundation.abhaservice.response.hip.BaseHIPModel;
import org.wishfoundation.abhaservice.response.hip.BaseHIPWebhookResponse;
import org.wishfoundation.abhaservice.response.hiu.BaseHIUModel;
import org.wishfoundation.abhaservice.service.HIPService;
import org.wishfoundation.abhaservice.service.HIUService;
import org.wishfoundation.abhaservice.utils.Helper;

import static org.wishfoundation.abhaservice.utils.Helper.MAPPER;

/**
 * This class is a REST controller for handling ABHA Webhook requests.
 * It handles various endpoints related to user authentication, consent management, and health information exchange.
 */
@RestController
@RequestMapping("/v0.5")
public class ABHAWebhookController {

    /**
     * Autowired instance of HIPService for handling HIP-related operations.
     */
    @Autowired
    private HIPService hIPService;

    /**
     * Autowired instance of HIUService for handling HIU-related operations.
     */
    @Autowired
    private HIUService hiuService;

    /**
     * Autowired instance of Helper for providing utility methods.
     */
    @Autowired
    private Helper helper;

    /**
     * This method handles the "on-init" webhook event for user authentication.
     * It logs the request, extracts necessary information, and confirms the authentication request.
     *
     * @param request The incoming webhook request.
     * @return A ResponseEntity with a null body.
     */
    @PostMapping("/users/auth/on-init")
    public ResponseEntity<BaseHIPWebhookResponse> onInit(@RequestBody BaseHIPWebhookRequest request) {

        try {
            System.out.println(" onInit " + MAPPER.writeValueAsString(request));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        BaseHIPWebhookRequest.Resp resp = request.getResp();
        BaseHIPWebhookRequest.Auth auth = request.getAuth();

        HIPConfirmRequest confirmRequest = new HIPConfirmRequest();
        // ref to redis key.
        confirmRequest.setRequestId(resp.getRequestId());
        confirmRequest.setTransactionId(auth.getTransactionId());
        try {
            ResponseEntity<BaseHIPModel> confim = hIPService.confim(confirmRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

    /**
     * This method handles the "on-confirm" webhook event for user authentication.
     * It logs the request, extracts necessary information, confirms the authentication request,
     * and adds the care context to the user's profile.
     *
     * @param request The incoming webhook request.
     * @return A ResponseEntity with a null body.
     */
    @PostMapping("/users/auth/on-confirm")
    public ResponseEntity<BaseHIPWebhookResponse> onConfirm(@RequestBody BaseHIPWebhookRequest request) {

        try {
            // Log the incoming webhook request
            System.out.println(" onConfirm " + MAPPER.writeValueAsString(request));

        } catch (JsonProcessingException e) {
            // Handle JSON processing exception
            e.printStackTrace();
        }

        // Extract authentication information from the request
        BaseHIPWebhookRequest.Auth auth = request.getAuth();
        String accessToken = auth.getAccessToken();

        // Generate the root Redis key based on the request ID
        System.out.println("request.getResp().getRequestId() : " + ABHAFlowChainRequest.HIP_KEY_PREFIX + request.getResp().getRequestId());
        String rootKey = helper.getRootRedisKey(ABHAFlowChainRequest.HIP_KEY_PREFIX + request.getResp().getRequestId());
        System.out.println("rootKey : " + rootKey);

        try {
            // Simulate a delay
            Thread.sleep(2000);

            // Retrieve and print all entries in the Redis hash for the root key
            helper.getRedisTemplate().opsForHash().entries(rootKey).entrySet().forEach(System.out::println);

            // Retrieve the care context ID from the Redis hash
            Object object = helper.getRedisTemplate().opsForHash().get(rootKey, ABHAFlowChainRequest.CARE_CONTEXT_KEY);
            System.out.println("=======================================" + String.valueOf(object));

            // Check if the care context ID is not empty
            if (!ObjectUtils.isEmpty(object)) {
                System.out.println("1--------------------------------------------");
                String careContextId = object.toString();
                System.out.println("care cintext id - ------------ " + careContextId);

                // Create a new HIP request object
                BaseHIPRequest baseHIPRequest = new BaseHIPRequest();
                System.out.println("creating base hip request ------------ ");

                // Set patient information and link access token in the HIP request
                baseHIPRequest.setPatient(request.getAuth().getPatient());
                Link link = new Link();
                link.setAccessToken(accessToken);
                baseHIPRequest.setLink(link);

                // Set the care context ID in the HIP request
                baseHIPRequest.setCareContextId(careContextId);

                try {
                    // Log the attempt to add the care context
                    System.out.println("trying to hit add care context ---------------- ");

                    // Set the request ID in the HIP request
                    baseHIPRequest.setRequestId(request.getResp().getRequestId());

                    // Call the HIP service to add the care context
                    hIPService.addCareContext(baseHIPRequest);

                } catch (Exception e) {
                    // Handle any exceptions that occur during the HIP service call
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during the execution of the method
            System.out.println("here---------------------- ");
            e.printStackTrace();
        }

        // Return a ResponseEntity with a null body
        return ResponseEntity.ok(null);
    }

    /**
     * This method handles the "notifyHip" webhook event for consent management.
     * It logs the request, extracts necessary information, and notifies the HIP about a consent request.
     *
     * @param request The incoming webhook request.
     * @return A BaseHIPWebhookResponse object containing the response from the HIP service.
     * @throws JsonProcessingException If there is an error while processing the JSON request.
     */
    @PostMapping("/consents/hip/notify")
    public BaseHIPWebhookResponse notifyHip(@RequestBody BaseHIPWebhookRequest request) {

        try {
            // Log the incoming webhook request
            System.out.println(" notifyHip " + MAPPER.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            // Handle JSON processing exception
            e.printStackTrace();
        }

        // Call the HIP service to handle the notifyHip event
        return hIPService.notifyHip(request);
    }

    /**
     * This method handles the "requestHip" webhook event for health information exchange.
     * It logs the request, extracts necessary information, and sends a request to the HIP.
     *
     * @param request The incoming webhook request.
     * @return A ResponseEntity with a BaseHIPWebhookResponse object containing the response from the HIP service.
     * @throws InterruptedException If the thread is interrupted while sleeping.
     */
    @PostMapping("/health-information/hip/request")
    public ResponseEntity<BaseHIPWebhookResponse> requestHip(@RequestBody BaseHIPWebhookRequest request) {

        try {
            // Simulate a delay of 2 seconds
            Thread.sleep(2000);

            // Log the incoming webhook request
            System.out.println(" requestHip " + MAPPER.writeValueAsString(request));

        } catch (InterruptedException e) {
            // Handle interruption of the sleeping thread
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // Handle JSON processing exception
            e.printStackTrace();
        }

        // Call the HIP service to handle the requestHip event
        return hIPService.requestHip(request);
    }


    /**
     * This method handles the "discover" webhook event for care context discovery.
     * It logs the request, extracts necessary information, and sends a request to the HIP.
     *
     * @param request The incoming webhook request containing the necessary information for care context discovery.
     * @return A BaseHIPWebhookResponse object containing the response from the HIP service.
     * @throws JsonProcessingException If there is an error while processing the JSON request.
     */
    @PostMapping("/care-contexts/discover")
    public BaseHIPWebhookResponse discover(@RequestBody BaseHIPWebhookRequest request) {
        try {
            // Log the incoming webhook request
            System.out.println(" discover " + MAPPER.writeValueAsString(request));
        } catch (Exception e) {
            // Handle any exceptions that occur during the JSON processing
            e.printStackTrace();
        }
        // Call the HIP service to handle the discover event
        return hIPService.discover(request);
    }

    /**
     * This method handles the "linkInit" webhook event for initiating a link between a user and a health information provider.
     * It logs the request, extracts necessary information, and sends a request to the HIP.
     *
     * @param request The incoming webhook request containing the necessary information for link initialization.
     * @return A BaseHIPWebhookResponse object containing the response from the HIP service.
     * @throws JsonProcessingException If there is an error while processing the JSON request.
     */
    @PostMapping("/links/link/init")
    public BaseHIPWebhookResponse linkInit(@RequestBody BaseHIPWebhookRequest request) {
        try {
            // Log the incoming webhook request
            System.out.println(" linkInit " + MAPPER.writeValueAsString(request));
        } catch (Exception e) {
            // Handle any exceptions that occur during the JSON processing
            e.printStackTrace();
        }
        // Call the HIP service to handle the linkInit event
        return hIPService.linkInit(request);
    }

    /**
     * This method handles the "linkConfirm" webhook event for confirming a link between a user and a health information provider.
     * It logs the request, extracts necessary information, and sends a request to the HIP.
     *
     * @param request The incoming webhook request containing the necessary information for link confirmation.
     * @return A BaseHIPWebhookResponse object containing the response from the HIP service.
     * @throws JsonProcessingException If there is an error while processing the JSON request.
     */
    @PostMapping("/links/link/confirm")
    public BaseHIPWebhookResponse confirm(@RequestBody BaseHIPWebhookRequest request) {
        try {
            // Log the incoming webhook request
            System.out.println(" confirm " + MAPPER.writeValueAsString(request));
        } catch (Exception e) {
            // Handle any exceptions that occur during the JSON processing
            e.printStackTrace();
        }
        // Call the HIP service to handle the linkConfirm event
        return hIPService.confirm(request);
    }


    /**
     * This method handles the "consent-requests/on-init" webhook event for the HIU (Health Information User).
     * It logs the request, extracts necessary information, and notifies the HIU about an incoming consent request.
     *
     * @param request The incoming webhook request containing the necessary information for consent initialization.
     * @return A ResponseEntity with a BaseHIUModel object containing the response from the HIU service.
     * @throws JsonProcessingException If there is an error while processing the JSON request.
     */
    @PostMapping("/consent-requests/on-init")
    public ResponseEntity<BaseHIUModel> consentOnInit(@RequestBody BaseHIPWebhookRequest request) {
        return hiuService.consentOnInit(request);
    }

    /**
     * This method handles the "consents/hiu/notify" webhook event for the HIU (Health Information User).
     * It logs the request, extracts necessary information, and notifies the HIU about an incoming consent request.
     *
     * @param request The incoming webhook request containing the necessary information for consent initialization.
     * @return A ResponseEntity with a BaseHIUModel object containing the response from the HIU service.
     * @throws JsonProcessingException If there is an error while processing the JSON request.
     */
    @PostMapping("/consents/hiu/notify")
    public ResponseEntity<BaseHIUModel> hiuNotify(@RequestBody BaseHIPWebhookRequest request) {
        return hiuService.hiuNotify(request);
    }

    /**
     * This method handles the "consents/on-fetch" webhook event for the HIU (Health Information User).
     * It logs the request, extracts necessary information, and notifies the HIU about an incoming consent request.
     *
     * @param request The incoming webhook request containing the necessary information for consent initialization.
     * @return A ResponseEntity with a BaseHIUModel object containing the response from the HIU service.
     * @throws JsonProcessingException If there is an error while processing the JSON request.
     */
    @RequestMapping("/consents/on-fetch")
    public ResponseEntity<BaseHIUModel> onFetch(@RequestBody BaseHIPWebhookRequest request) {
        return hiuService.onFetch(request);
    }

    /**
     * This method handles the "health-information/hiu/on-request" webhook event for the HIU (Health Information User).
     * It logs the request, extracts necessary information, and notifies the HIU about an incoming health information request.
     *
     * @param request The incoming webhook request containing the necessary information for health information request.
     * @return A ResponseEntity with a BaseHIUModel object containing the response from the HIU service.
     * @throws JsonProcessingException If there is an error while processing the JSON request.
     */
    @RequestMapping("/health-information/hiu/on-request")
    public ResponseEntity<BaseHIUModel> onRequest(@RequestBody BaseHIPWebhookRequest request) {
        return hiuService.onRequest(request);
    }


}
