package org.wishfoundation.userservice.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MedicalsReportsResponse {

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
