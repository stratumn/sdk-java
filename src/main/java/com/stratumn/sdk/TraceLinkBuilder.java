/*
Copyright 2017 Stratumn SAS. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.stratumn.sdk;

import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.stratumn.canonicaljson.CanonicalJson;
import com.stratumn.chainscript.ChainscriptException;
import com.stratumn.chainscript.Link;
import com.stratumn.chainscript.LinkBuilder;
import com.stratumn.chainscript.utils.CryptoUtils;
import com.stratumn.sdk.model.trace.ITraceLink;
import com.stratumn.sdk.model.trace.TraceActionType;
import com.stratumn.sdk.model.trace.TraceLinkBuilderConfig;
import com.stratumn.sdk.model.trace.TraceLinkMetaData;
import com.stratumn.sdk.model.trace.TraceLinkType;

/**
 * TraceLinkBuilder makes it easy to create links that are compatible
 * with Trace.
 * It provides valid default values for required fields and allows the user
 * to set fields to valid values.
 */
public class TraceLinkBuilder<TLinkData> extends LinkBuilder {

	private TraceLinkMetaData metadata;

	private ITraceLink<TLinkData> parentLink;

	private TLinkData formData;
 
	/**
	 * Create a new instance of a TraceLinkBuilder.
	 *
	 * If a parent link is provided, then the trace id and priority will be
	 * calculated from it.
	 *
	 * If no parent link is provided, then it is assumed that the link will be the
	 * first of a new trace and priority is set to 1.
	 *
	 * @param cfg the config to instantiate the builder
	 * @throws Exception 
	 */

	public TraceLinkBuilder(TraceLinkBuilderConfig<TLinkData> cfg) throws Exception {
 
	   // trace id is either retrieved from parent link when it is provided
	    // or set to a new uuid.
		super(cfg.getWorkflowId(), 
				cfg.getParentLink()!=null?cfg.getParentLink().traceId():UUID.randomUUID().toString());
		
		  // set the parent link
        this.parentLink = (TraceLink<TLinkData>)cfg.getParentLink();
		
        // degree is always 1
        super.withDegree(1);
        // set priority to 1 by default
        // may be overriden if parent link was provided
        super.withPriority(1);
        
        // set the created at timestamp 
		this.metadata=new TraceLinkMetaData(); 
		this.metadata.setCreatedAt(new Date());
 
        // if parent link was provided set the parent hash and priority
        if (this.parentLink != null) {
            super
                // increment the priority by 1
                .withPriority(this.parentLink.priority() + 1)
                // use parent link hash
                .withParent(this.parentLink.hash());
        }

	}


	/**
	 * Helper method to get the parent link. Will throw if no parent link was
	 * provided.
	 * 
	 * @throws Exception
	 */
	public TraceLink<TLinkData> getParentLink() throws TraceSdkException {
		if (this.parentLink == null) {
			throw new TraceSdkException("Parent link must be provided");
		}
		return (TraceLink<TLinkData>) this.parentLink;
	}

	/**
	 * Set the data field to the hash of the object argument.
	 *
	 * @param obj the optional object to be hashed
	 * @throws IOException
	 */
	public TraceLinkBuilder<TLinkData> withHashedData(TLinkData obj) throws IOException {
		if (obj != null) {
			String algo = "sha256";
			String hash = Base64.getEncoder()
					.encodeToString(CryptoUtils.sha256(CanonicalJson.stringify(obj).getBytes()));

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("algo", algo);
			data.put("hash", hash);
			this.withData(data);
			this.formData = obj;
		}
		return this;
	}

	/**
	 * Helper method used to configure a link for an attestation. User must still
	 * set owner, group and createdBy separately.
	 *
	 * @param formId the form id used for the attestation
	 * @param action the name of the action associated with this form
	 * @param data   the data of the attestation
	 * @throws IOException 
	 */
	public TraceLinkBuilder<TLinkData> forAttestation(String formId,String action , TLinkData data) throws IOException {
	    String actionStr =action != null ?action  : "Attestation"  ;//TraceActionType.ATTESTATION.toString();
		String typeStr = TraceLinkType.OWNED.toString() ;
		this.withHashedData(data).withAction(actionStr).withProcessState(typeStr);
		this.metadata.setFormId(formId);
		return this;
	}

	/**
	 * Helper method used for transfer of ownership requests (push and pull). Note
	 * that owner and group are calculated from parent link. Parent link must have
	 * been provided!
	 *
	 * @param to     the group to which the transfer is made for
	 * @param action the action (_PUSH_OWNERSHIP_ or _PULL_OWNERSHIP_)
	 * @param type   the type (PUSHING OR PULLING)
	 * @param data   the optional data
	 * @throws Exception 
	 */
	public TraceLinkBuilder<TLinkData> forTransferRequest(String to, TraceActionType action, TraceLinkType type, TLinkData data) throws Exception {
		TraceLink<TLinkData> parent = this.getParentLink();
		this.withOwner(parent.owner().getAccount())
		.withGroup(parent.group())
		.withHashedData(data)
		.withAction(action.toString())
		.withProcessState(type.toString());

		this.metadata.setInputs(new String[] { to });
		this.metadata.setLastFormId(parent.form() != null ? parent.form() : parent.lastForm());
		return this;
	}

	/**
	 * Helper method used for pushing ownership to another group.
	 *
	 * @param to   the group to which the trace is pushed to
	 * @param data the optional data
	 * @throws Exception 
	 */
	public TraceLinkBuilder<TLinkData> forPushTransfer(String to, TLinkData data) throws Exception {
		return this.forTransferRequest(to, TraceActionType.PUSH_OWNERSHIP, TraceLinkType.PUSHING, data);
	}

	/**
	 * Helper method used for pulling ownership from another group.
	 *
	 * @param to   the group to which the trace is pulled to
	 * @param data the optional data
	 * @throws Exception 
	 */
	public TraceLinkBuilder<TLinkData> forPullTransfer(String to, TLinkData data) throws Exception {
		return this.forTransferRequest(to, TraceActionType.PULL_OWNERSHIP, TraceLinkType.PULLING, data);
	}

	/**
	 * Helper method used to cancel a transfer request. Note that owner and group
	 * are calculated from parent link. Parent link must have been provided!
	 *
	 * @param data the optional data
	 * @throws Exception 
	 */
	public TraceLinkBuilder<TLinkData> forCancelTransfer(TLinkData data) throws Exception {
		TraceLink<TLinkData> parent = this.getParentLink();
		String action =TraceActionType.CANCEL_TRANSFER.toString();
		String type = TraceLinkType.OWNED.toString();
		this.withOwner(parent.owner().getAccount()).withGroup(parent.group()).withHashedData(data).withAction(action)
				.withProcessState(type);
		return this;
	}

	/**
	 * Helper method used to reject a transfer request. Note that owner and group
	 * are calculated from parent link. Parent link must have been provided!
	 *
	 * @param data the optional data
	 * @throws Exception 
	 */
	public TraceLinkBuilder<TLinkData> forRejectTransfer(TLinkData data) throws Exception {
		TraceLink<TLinkData> parent = this.getParentLink();
		String action =TraceActionType.REJECT_TRANSFER.toString();
        String type = TraceLinkType.OWNED.toString();
		 
		this.withOwner(parent.owner().getAccount()).withGroup(parent.group()).withHashedData(data).withAction(action)
				.withProcessState(type);
		return this;
	}

	/**
	 * Helper method used to accept a transfer request. Parent link must have been
	 * provided! User must still set owner, group and createdBy separately.
	 *
	 * @param data the optional data
	 * @throws Exception 
	 */
	public TraceLinkBuilder<TLinkData> forAcceptTransfer(TLinkData data) throws Exception {
		// call parent link to assert it was set
		this.getParentLink();
		String action =TraceActionType.ACCEPT_TRANSFER.toString();
        String type = TraceLinkType.OWNED.toString();
		 
		this.withHashedData(data).withAction(action).withProcessState(type);
		return this;
	}

	/**
	 * To set the metadata ownerId.
	 *
	 * @param ownerId the owner id
	 */
	public TraceLinkBuilder<TLinkData> withOwner(String ownerId) {
		this.metadata.setOwnerId(ownerId);
		return this;
	}

	/**
	 * To set the metadata groupId.
	 *
	 * @param groupId the group id
	 */
	public TraceLinkBuilder<TLinkData> withGroup(String groupId) {
		this.metadata.setGroupId( groupId);
		return this;
	}

	/**
	 * To set the metadata createdById.
	 *
	 * @param userId the user id
	 * @return
	 */
	public TraceLinkBuilder<TLinkData> withCreatedBy(String userId) {
		this.metadata.setCreatedById(userId);
		return this;
	}

	@Override
   public TraceLink<TLinkData> build() throws ChainscriptException
   {
      super.withMetadata(this.metadata);
      Link link = super.build();
      return new TraceLink<TLinkData>(link, (TLinkData) this.formData);

   }

	 
	
}
