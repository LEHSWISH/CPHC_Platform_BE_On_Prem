package org.wishfoundation.userservice.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicalsReportsRequest {

	private boolean heartDisease;
	private boolean hypertension;
	private boolean respiratoryDiseaseOrAsthma;
	private boolean diabetesMellitus;
	private boolean tuberculosis;
	private boolean epilepsyOrAnyNeurologicalDisorder;
	private boolean kidneyOrUrinaryDisorder;
	private boolean cancer;
	private boolean migraineOrPersistentHeadache;
	private boolean anyAllergies;
	private boolean disorderOfTheJointsOrMusclesArthritisGout;
	private boolean anyMajorSurgery;
	private boolean noneOfTheAbove;
}
