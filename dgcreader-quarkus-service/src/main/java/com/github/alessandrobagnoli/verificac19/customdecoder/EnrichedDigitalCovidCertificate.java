package com.github.alessandrobagnoli.verificac19.customdecoder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.digg.dgc.payload.v1.DGCSchemaException;
import se.digg.dgc.payload.v1.DigitalCovidCertificate;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedDigitalCovidCertificate extends DigitalCovidCertificate {

    @JsonProperty("e")
    @JsonPropertyDescription("Exemption Group")
    @Size(min = 1, max = 1)
    @Valid
    private List<ExemptionEntry> e;

    public static EnrichedDigitalCovidCertificate decode(final byte[] cbor)
        throws DGCSchemaException {
        try {
            return getCBORMapper().readValue(cbor, EnrichedDigitalCovidCertificate.class);
        } catch (final IOException e) {
            throw new DGCSchemaException("Failed to decode DCC from CBOR encoding", e);
        }
    }

    /**
     * Exemption Group
     */
    @JsonProperty("e")
    public List<ExemptionEntry> getE() {
        return e;
    }

    /**
     * Exemption Group
     */
    @JsonProperty("e")
    public void setE(List<ExemptionEntry> e) {
        this.e = e;
    }

    public EnrichedDigitalCovidCertificate withE(List<ExemptionEntry> e) {
        this.e = e;
        return this;
    }

    @Data
    @Builder
    public static class ExemptionEntry {
        /**
         * EU eHealthNetwork: Value Sets for Digital Covid Certificates. version 1.0, 2021-04-16, section 2.1
         * (Required)
         */
        @JsonProperty("tg")
        @JsonPropertyDescription("EU eHealthNetwork: Value Sets for Digital Covid Certificates. version 1.0, 2021-04-16, section 2.1")
        @NotNull
        private String tg;
        /**
         * Country of Vaccination / Test, ISO 3166 alpha-2 where possible
         * (Required)
         */
        @JsonProperty("co")
        @JsonPropertyDescription("Country of Vaccination / Test, ISO 3166 alpha-2 where possible")
        @Pattern(regexp = "[A-Z]{1,10}")
        @NotNull
        private String co;
        /**
         * Certificate Issuer
         * (Required)
         */
        @JsonProperty("is")
        @JsonPropertyDescription("Certificate Issuer")
        @Size(max = 80)
        @NotNull
        private String is;
        /**
         * ISO 8601 complete date: Certificate Valid From
         * (Required)
         */
        @JsonProperty("df")
        @JsonPropertyDescription("ISO 8601 complete date: Certificate Valid From")
        @NotNull
        private LocalDate df;
        /**
         * ISO 8601 complete date: Certificate Valid Until
         * (Required)
         */
        @JsonProperty("du")
        @JsonPropertyDescription("ISO 8601 complete date: Certificate Valid Until")
        @NotNull
        private LocalDate du;
        /**
         * Certificate Identifier, format as per UVCI: Annex 2 in  https://ec.europa.eu/health/sites/health/files/ehealth/docs/vaccination-proof_interoperability-guidelines_en.pdf
         * (Required)
         */
        @JsonProperty("ci")
        @JsonPropertyDescription("Certificate Identifier, format as per UVCI: Annex 2 in  https://ec.europa.eu/health/sites/health/files/ehealth/docs/vaccination-proof_interoperability-guidelines_en.pdf")
        @Size(max = 80)
        @NotNull
        private String ci;
    }
}
