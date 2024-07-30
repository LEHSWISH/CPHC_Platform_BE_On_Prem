package org.wishfoundation.abhaservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wishfoundation.abhaservice.entity.Consent;
import org.wishfoundation.abhaservice.entity.ConsentArtefact;
import org.wishfoundation.abhaservice.request.hiu.BaseHIURequest;
import org.wishfoundation.abhaservice.request.hiu.FhirBundle;
import org.wishfoundation.abhaservice.request.webhook.BaseHIPWebhookRequest;
import org.wishfoundation.abhaservice.response.hiu.BaseHIUModel;
import org.wishfoundation.abhaservice.service.HIUService;

import java.util.List;

/**
 * This class is a REST controller for handling requests from the ABHA HIU.
 * It provides endpoints for consent initiation, consent fetching, health information request,
 * and data fetching from the consent database and consent artefacts.
 * It also handles data push requests from the Health Information Platform (HIP) using webhooks.
 */
@RestController
@RequestMapping("/api/v1/abha-hiu")
public class HIUController {

    /**
     * Autowired instance of HIUService.
     */
    @Autowired
    private HIUService hiuService;

    /**
     * Endpoint for initiating consent requests.
     *
     * @param request The request object containing necessary parameters.
     * @return ResponseEntity containing the response model.
     */
    @PostMapping("/consent-requests/init")
    public ResponseEntity<BaseHIUModel> consentInit(@RequestBody BaseHIURequest request) {
        try {
            return hiuService.consentInit(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Endpoint for fetching consents.
     *
     * @param request The request object containing necessary parameters.
     * @return ResponseEntity containing the response model.
     */
    @PostMapping("/consents/fetch")
    public ResponseEntity<BaseHIUModel> fetchConsent(@RequestBody BaseHIURequest request) {
        return hiuService.fetchConsent(request);
    }

    /**
     * Endpoint for requesting health information.
     *
     * @param request The request object containing necessary parameters.
     * @return ResponseEntity containing the response model.
     */
    @PostMapping("/health-information/cm/request")
    public ResponseEntity<BaseHIUModel> requestDocument(@RequestBody BaseHIURequest request) {
        return hiuService.requestDocument(request);
    }

    /**
     * Endpoint for fetching consents from the consent database.
     *
     * @param request The request object containing necessary parameters.
     * @return ResponseEntity containing a list of Consent objects.
     */
    @PostMapping("/fetch/consent-db")
    public ResponseEntity<List<Consent>> fetchConsentDB(@RequestBody BaseHIURequest request) {
        return hiuService.fetchConsentDB(request);
    }

    /**
     * Endpoint for fetching consent artefacts from the consent database.
     *
     * @param request The request object containing necessary parameters.
     * @return ResponseEntity containing a list of ConsentArtefact objects.
     */
    @PostMapping("/fetch/consent-artefacts-db")
    public ResponseEntity<List<ConsentArtefact>> fetchConsentArtefact(@RequestBody BaseHIURequest request) {
        return hiuService.fetchConsentArtefacts(request);
    }

    /**
     * Endpoint for handling data push requests from the HIP using webhooks.
     *
     * @param request The webhook request object containing necessary parameters.
     * @return ResponseEntity containing a list of ConsentArtefact objects.
     */
    @PostMapping("/data-push")
    public ResponseEntity<List<ConsentArtefact>> dataPushDurl(@RequestBody BaseHIPWebhookRequest request) {
        return hiuService.dataPushDurl(request);
    }

    /**
     * Endpoint for retrieving FHIR bundle for a given HIP ID.
     *
     * @param hipId The HIP ID for which to retrieve the FHIR bundle.
     * @return A list of FhirBundle objects.
     */
    @GetMapping(path = "/get-fhir-bundle/hip/{hip-id}")
    public List<FhirBundle> getFHIRBundle(@PathVariable("hip-id") String hipId){
        return hiuService.getFHIRBundle(hipId);
    }
}
