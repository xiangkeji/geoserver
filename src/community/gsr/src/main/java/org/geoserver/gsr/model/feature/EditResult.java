/* (c) 2020 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

/* Copyright (c) 2017 Boundless - http://boundlessgeo.com All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */

package org.geoserver.gsr.model.feature;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.geoserver.gsr.model.exception.ServiceError;

/**
 * The result of an Add, Update, or Delete operation
 *
 * <p>See https://developers.arcgis.com/rest/services-reference/edit-result-object.htm
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditResult {

    public final Long objectId;
    public final Boolean success;
    public final ServiceError error;

    /**
     * Constructs a successful EditResult
     *
     * @param objectId id of the object
     */
    public EditResult(Long objectId) {
        this(objectId, true, null);
    }

    /**
     * Constructs an arbitrary EditResult
     *
     * @param objectId id of the object. In the case of a failed add, should be null
     * @param success result of the modify operation
     * @param error error object, if success=false
     */
    public EditResult(Long objectId, Boolean success, ServiceError error) {
        if (objectId == null && success) {
            throw new NullPointerException("objectId must not be null on success");
        }
        this.objectId = objectId;
        this.success = success;
        this.error = error;
    }

    public Long getObjectId() {
        return objectId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public ServiceError getError() {
        return error;
    }
}
