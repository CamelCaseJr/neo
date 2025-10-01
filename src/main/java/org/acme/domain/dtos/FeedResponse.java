
package org.acme.domain.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedResponse {
    @JsonProperty("element_count")
    public int elementCount;
    @JsonProperty("near_earth_objects")
    public Map<String, List<Neo>> nearEarthObjects;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Neo {
        @JsonProperty("id") public String id;
        @JsonProperty("name") public String name;
        @JsonProperty("absolute_magnitude_h") public Double absoluteMagnitudeH;
        @JsonProperty("is_potentially_hazardous_asteroid") public boolean hazardous;

        @JsonProperty("estimated_diameter")
        public EstimatedDiameter estimatedDiameter;

        @JsonProperty("close_approach_data")
        public List<CloseApproachData> closeApproachData;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EstimatedDiameter {
        public Range meters;
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Range {
            @JsonProperty("estimated_diameter_min") public Double min;
            @JsonProperty("estimated_diameter_max") public Double max;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CloseApproachData {
        @JsonProperty("close_approach_date_full") public String approachDateFull;
        @JsonProperty("orbiting_body") public String orbitingBody;
        @JsonProperty("relative_velocity") public RelativeVelocity relativeVelocity;
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class RelativeVelocity {
            @JsonProperty("kilometers_per_second") public String kmPerSec;
        }
    }
}
