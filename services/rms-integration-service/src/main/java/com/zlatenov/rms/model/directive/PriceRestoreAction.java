package com.zlatenov.rms.model.directive;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.retail.messaging.model.MessageMetadata;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Angel Zlatenov
 */

@Getter
@Setter
public class PriceRestoreAction {
    @NotNull
    private String eventId;

    @NotNull
    @Size(min = 1, max = 50)
    private String skuId;

    @NotNull
    private String locationId;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime effectiveDate;

    @NotNull
    private ActionCode actionCode;

    private MessageMetadata metadata;

}
