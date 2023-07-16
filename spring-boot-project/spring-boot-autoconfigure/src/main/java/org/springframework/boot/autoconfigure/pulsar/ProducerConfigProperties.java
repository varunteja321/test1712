/*
 * Copyright 2023-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.pulsar;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.CompressionType;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.ProducerAccessMode;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;

import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.pulsar.core.ProducerBuilderCustomizer;
import org.springframework.util.unit.DataSize;

/**
 * Configuration properties used to specify Pulsar producers.
 *
 * @author Chris Bono
 * @since 3.2.0
 */
public class ProducerConfigProperties {

	/**
	 * Topic the producer will publish to.
	 */
	private String topicName;

	/**
	 * Name for the producer. If not assigned, a unique name is generated.
	 */
	private String producerName;

	/**
	 * Time before a message has to be acknowledged by the broker.
	 */
	private Duration sendTimeout = Duration.ofSeconds(30);

	/**
	 * Whether the "send" and "sendAsync" methods should block if the outgoing message
	 * queue is full.
	 */
	private Boolean blockIfQueueFull = false;

	/**
	 * Maximum number of pending messages for the producer.
	 */
	private Integer maxPendingMessages = 1000;

	/**
	 * Maximum number of pending messages across all the partitions.
	 */
	private Integer maxPendingMessagesAcrossPartitions = 50000;

	/**
	 * Message routing mode for a partitioned producer.
	 */
	private MessageRoutingMode messageRoutingMode = MessageRoutingMode.RoundRobinPartition;

	/**
	 * Message hashing scheme to choose the partition to which the message is published.
	 */
	private HashingScheme hashingScheme = HashingScheme.JavaStringHash;

	/**
	 * Action the producer will take in case of encryption failure.
	 */
	private ProducerCryptoFailureAction cryptoFailureAction = ProducerCryptoFailureAction.FAIL;

	/**
	 * Time period within which the messages sent will be batched.
	 */
	private Duration batchingMaxPublishDelay = Duration.ofMillis(1);

	/**
	 * Partition switch frequency while batching of messages is enabled and using
	 * round-robin routing mode for non-keyed message.
	 */
	private Integer batchingPartitionSwitchFrequencyByPublishDelay = 10;

	/**
	 * Maximum number of messages to be batched.
	 */
	private Integer batchingMaxMessages = 1000;

	/**
	 * Maximum number of bytes permitted in a batch.
	 */
	private DataSize batchingMaxBytes = DataSize.ofKilobytes(128);

	/**
	 * Whether to automatically batch messages.
	 */
	private Boolean batchingEnabled = true;

	/**
	 * Whether to split large-size messages into multiple chunks.
	 */
	private Boolean chunkingEnabled = false;

	/**
	 * Names of the public encryption keys to use when encrypting data.
	 */
	private Set<String> encryptionKeys = new HashSet<>();

	/**
	 * Message compression type.
	 */
	private CompressionType compressionType;

	/**
	 * Baseline for the sequence ids for messages published by the producer.
	 */
	private Long initialSequenceId;

	/**
	 * Whether partitioned producer automatically discover new partitions at runtime.
	 */
	private Boolean autoUpdatePartitions = true;

	/**
	 * Interval of partitions discovery updates.
	 */
	private Duration autoUpdatePartitionsInterval = Duration.ofMinutes(1);

	/**
	 * Whether the multiple schema mode is enabled.
	 */
	private Boolean multiSchema = true;

	/**
	 * Type of access to the topic the producer requires.
	 */
	private ProducerAccessMode producerAccessMode = ProducerAccessMode.Shared;

	/**
	 * Whether producers in Shared mode register and connect immediately to the owner
	 * broker of each partition or start lazily on demand.
	 */
	private Boolean lazyStartPartitionedProducers = false;

	/**
	 * Map of properties to add to the producer.
	 */
	private Map<String, String> properties = new HashMap<>();

	private final PulsarProperties.Cache cache = new PulsarProperties.Cache();

	public String getTopicName() {
		return this.topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getProducerName() {
		return this.producerName;
	}

	public void setProducerName(String producerName) {
		this.producerName = producerName;
	}

	public Duration getSendTimeout() {
		return this.sendTimeout;
	}

	public void setSendTimeout(Duration sendTimeout) {
		this.sendTimeout = sendTimeout;
	}

	public Boolean getBlockIfQueueFull() {
		return this.blockIfQueueFull;
	}

	public void setBlockIfQueueFull(Boolean blockIfQueueFull) {
		this.blockIfQueueFull = blockIfQueueFull;
	}

	public Integer getMaxPendingMessages() {
		return this.maxPendingMessages;
	}

	public void setMaxPendingMessages(Integer maxPendingMessages) {
		this.maxPendingMessages = maxPendingMessages;
	}

	public Integer getMaxPendingMessagesAcrossPartitions() {
		return this.maxPendingMessagesAcrossPartitions;
	}

	public void setMaxPendingMessagesAcrossPartitions(Integer maxPendingMessagesAcrossPartitions) {
		this.maxPendingMessagesAcrossPartitions = maxPendingMessagesAcrossPartitions;
	}

	public MessageRoutingMode getMessageRoutingMode() {
		return this.messageRoutingMode;
	}

	public void setMessageRoutingMode(MessageRoutingMode messageRoutingMode) {
		this.messageRoutingMode = messageRoutingMode;
	}

	public HashingScheme getHashingScheme() {
		return this.hashingScheme;
	}

	public void setHashingScheme(HashingScheme hashingScheme) {
		this.hashingScheme = hashingScheme;
	}

	public ProducerCryptoFailureAction getCryptoFailureAction() {
		return this.cryptoFailureAction;
	}

	public void setCryptoFailureAction(ProducerCryptoFailureAction cryptoFailureAction) {
		this.cryptoFailureAction = cryptoFailureAction;
	}

	public Duration getBatchingMaxPublishDelay() {
		return this.batchingMaxPublishDelay;
	}

	public void setBatchingMaxPublishDelay(Duration batchingMaxPublishDelay) {
		this.batchingMaxPublishDelay = batchingMaxPublishDelay;
	}

	public Integer getBatchingPartitionSwitchFrequencyByPublishDelay() {
		return this.batchingPartitionSwitchFrequencyByPublishDelay;
	}

	public void setBatchingPartitionSwitchFrequencyByPublishDelay(
			Integer batchingPartitionSwitchFrequencyByPublishDelay) {
		this.batchingPartitionSwitchFrequencyByPublishDelay = batchingPartitionSwitchFrequencyByPublishDelay;
	}

	public Integer getBatchingMaxMessages() {
		return this.batchingMaxMessages;
	}

	public void setBatchingMaxMessages(Integer batchingMaxMessages) {
		this.batchingMaxMessages = batchingMaxMessages;
	}

	public DataSize getBatchingMaxBytes() {
		return this.batchingMaxBytes;
	}

	public void setBatchingMaxBytes(DataSize batchingMaxBytes) {
		this.batchingMaxBytes = batchingMaxBytes;
	}

	public Boolean getBatchingEnabled() {
		return this.batchingEnabled;
	}

	public void setBatchingEnabled(Boolean batchingEnabled) {
		this.batchingEnabled = batchingEnabled;
	}

	public Boolean getChunkingEnabled() {
		return this.chunkingEnabled;
	}

	public void setChunkingEnabled(Boolean chunkingEnabled) {
		this.chunkingEnabled = chunkingEnabled;
	}

	public Set<String> getEncryptionKeys() {
		return this.encryptionKeys;
	}

	public void setEncryptionKeys(Set<String> encryptionKeys) {
		this.encryptionKeys = encryptionKeys;
	}

	public CompressionType getCompressionType() {
		return this.compressionType;
	}

	public void setCompressionType(CompressionType compressionType) {
		this.compressionType = compressionType;
	}

	public Long getInitialSequenceId() {
		return this.initialSequenceId;
	}

	public void setInitialSequenceId(Long initialSequenceId) {
		this.initialSequenceId = initialSequenceId;
	}

	public Boolean getAutoUpdatePartitions() {
		return this.autoUpdatePartitions;
	}

	public void setAutoUpdatePartitions(Boolean autoUpdatePartitions) {
		this.autoUpdatePartitions = autoUpdatePartitions;
	}

	public Duration getAutoUpdatePartitionsInterval() {
		return this.autoUpdatePartitionsInterval;
	}

	public void setAutoUpdatePartitionsInterval(Duration autoUpdatePartitionsInterval) {
		this.autoUpdatePartitionsInterval = autoUpdatePartitionsInterval;
	}

	public Boolean getMultiSchema() {
		return this.multiSchema;
	}

	public void setMultiSchema(Boolean multiSchema) {
		this.multiSchema = multiSchema;
	}

	public ProducerAccessMode getProducerAccessMode() {
		return this.producerAccessMode;
	}

	public void setProducerAccessMode(ProducerAccessMode producerAccessMode) {
		this.producerAccessMode = producerAccessMode;
	}

	public Boolean getLazyStartPartitionedProducers() {
		return this.lazyStartPartitionedProducers;
	}

	public void setLazyStartPartitionedProducers(Boolean lazyStartPartitionedProducers) {
		this.lazyStartPartitionedProducers = lazyStartPartitionedProducers;
	}

	public Map<String, String> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public PulsarProperties.Cache getCache() {
		return this.cache;
	}

	@SuppressWarnings("deprecation")
	public ProducerBuilderCustomizer<?> toProducerBuilderCustomizer() {
		return (producerBuilder) -> {
			PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
			map.from(this::getTopicName).to(producerBuilder::topic);
			map.from(this::getProducerName).to(producerBuilder::producerName);
			map.from(this::getSendTimeout)
				.asInt(Duration::toMillis)
				.to(producerBuilder, (pb, val) -> pb.sendTimeout(val, TimeUnit.MILLISECONDS));
			map.from(this::getBlockIfQueueFull).to(producerBuilder::blockIfQueueFull);
			map.from(this::getMaxPendingMessages).to(producerBuilder::maxPendingMessages);
			map.from(this::getMaxPendingMessagesAcrossPartitions)
				.to(producerBuilder::maxPendingMessagesAcrossPartitions);
			map.from(this::getMessageRoutingMode).to(producerBuilder::messageRoutingMode);
			map.from(this::getHashingScheme).to(producerBuilder::hashingScheme);
			map.from(this::getCryptoFailureAction).to(producerBuilder::cryptoFailureAction);
			map.from(this::getBatchingMaxPublishDelay)
				.as(Duration::toMillis)
				.to(producerBuilder, (pb, val) -> pb.batchingMaxPublishDelay(val, TimeUnit.MILLISECONDS));
			map.from(this::getBatchingPartitionSwitchFrequencyByPublishDelay)
				.to(producerBuilder::roundRobinRouterBatchingPartitionSwitchFrequency);
			map.from(this::getBatchingMaxMessages).to(producerBuilder::batchingMaxMessages);
			map.from(this::getBatchingMaxBytes).asInt(DataSize::toBytes).to(producerBuilder::batchingMaxBytes);
			map.from(this::getBatchingEnabled).to(producerBuilder::enableBatching);
			map.from(this::getChunkingEnabled).to(producerBuilder::enableChunking);
			map.from(this::getEncryptionKeys)
				.to((encryptionKeys) -> encryptionKeys.forEach(producerBuilder::addEncryptionKey));
			map.from(this::getCompressionType).to(producerBuilder::compressionType);
			map.from(this::getInitialSequenceId).to(producerBuilder::initialSequenceId);
			map.from(this::getAutoUpdatePartitions).to(producerBuilder::autoUpdatePartitions);
			map.from(this::getAutoUpdatePartitionsInterval)
				.asInt(Duration::toMillis)
				.to(producerBuilder, (pb, val) -> pb.autoUpdatePartitionsInterval(val, TimeUnit.MILLISECONDS));
			map.from(this::getMultiSchema).to(producerBuilder::enableMultiSchema);
			map.from(this::getProducerAccessMode).to(producerBuilder::accessMode);
			map.from(this::getLazyStartPartitionedProducers).to(producerBuilder::enableLazyStartPartitionedProducers);
			map.from(this::getProperties).to(producerBuilder::properties);
		};
	}

}