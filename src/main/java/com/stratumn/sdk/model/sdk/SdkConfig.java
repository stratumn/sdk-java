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
package com.stratumn.sdk.model.sdk;

import java.security.PrivateKey;

public class SdkConfig {

	/**
	 * The workflow id
	 */
	private String workflowId;

	/**
	 * The workflow config id
	 */
	private String configId;
	/**
	 * The user id
	 */
	private String userId;
	/**
	 * The account id
	 */
	private String accountId;
	/**
	 * The group id
	 */
	private String groupId;

	/**
	 * The owner id
	 */
	private String ownerId;
	/**
	 * The private key used for signing links
	 */
	private PrivateKey signingPrivateKey;

	public SdkConfig() {
	}

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public SdkConfig(String workflowId, String configId, String userId, String accountId, String groupId, String ownerId,
			PrivateKey signingPrivateKey) {
		this.workflowId = workflowId;
		this.configId = configId;
		this.userId = userId;
		this.accountId = accountId;
		this.groupId = groupId;
		this.ownerId = ownerId;
		this.signingPrivateKey = signingPrivateKey;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public PrivateKey getSigningPrivateKey() {
		return signingPrivateKey;
	}

	public void setSigningPrivateKey(PrivateKey signingPrivateKey) {
		this.signingPrivateKey = signingPrivateKey;
	}

}
