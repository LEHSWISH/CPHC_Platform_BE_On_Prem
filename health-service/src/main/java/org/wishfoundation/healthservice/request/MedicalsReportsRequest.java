package org.wishfoundation.healthservice.request;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
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
