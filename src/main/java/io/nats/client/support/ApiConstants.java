// Copyright 2020 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.client.support;

public interface ApiConstants {

    String ACK_FLOOR         = "ack_floor";
    String ACK_POLICY        = "ack_policy";
    String ACK_WAIT          = "ack_wait";
    String ACTIVE            = "active";
    String ALLOW_ROLLUP_HDRS = "allow_rollup_hdrs";
    String ALLOW_DIRECT      = "allow_direct";
    String AVERAGE_PROCESSING_TIME = "average_processing_time";
    String MIRROR_DIRECT     = "mirror_direct";
    String API               = "api";
    String API_URL           = "api_url";
    String AUTH_REQUIRED     = "auth_required";
    String BACKOFF           = "backoff";
    String BATCH             = "batch";
    String BUCKET            = "bucket";
    String BYTES             = "bytes";
    String CHUNKS            = "chunks";
    String CLIENT_ID         = "client_id";
    String CLIENT_IP         = "client_ip";
    String CLUSTER           = "cluster";
    String CODE              = "code";
    String CONFIG            = "config";
    String CONNECT_URLS      = "connect_urls";
    String CONSUMER_COUNT    = "consumer_count";
    String CONSUMER_SEQ      = "consumer_seq";
    String CONSUMERS         = "consumers";
    String CREATED           = "created";
    String CURRENT           = "current";
    String DATA              = "data";
    String DELETED           = "deleted";
    String DELETED_DETAILS   = "deleted_details";
    String DELIVER           = "deliver";
    String DELIVER_GROUP     = "deliver_group";
    String DELIVER_POLICY    = "deliver_policy";
    String DELIVER_SUBJECT   = "deliver_subject";
    String DELIVERED         = "delivered";
    String DENY_DELETE       = "deny_delete";
    String DENY_PURGE        = "deny_purge";
    String DESCRIPTION       = "description";
    String DEST              = "dest";
    String DIGEST            = "digest";
    String DISCARD           = "discard";
    String DISCARD_NEW_PER_SUBJECT = "discard_new_per_subject";
    String DOMAIN            = "domain";
    String DUPLICATE         = "duplicate";
    String DUPLICATE_WINDOW  = "duplicate_window";
    String ENDPOINTS         = "endpoints";
    String DURABLE_NAME      = "durable_name";
    String ERR_CODE          = "err_code";
    String ERROR             = "error";
    String ERRORS            = "errors";
    String EXPIRES           = "expires";
    String EXTERNAL          = "external";
    String FILTER            = "filter";
    String FILTER_SUBJECT    = "filter_subject";
    String FILTER_SUBJECTS   = "filter_subjects";
    String FIRST_SEQ         = "first_seq";
    String FIRST_TS          = "first_ts";
    String FLOW_CONTROL      = "flow_control";
    String GO                = "go";
    String HDRS              = "hdrs";
    String HEADERS           = "headers";
    String HEADERS_ONLY      = "headers_only";
    String HOST              = "host";
    String ID                = "id";
    String IDLE_HEARTBEAT    = "idle_heartbeat";
    String INACTIVE_THRESHOLD= "inactive_threshold";
    String INTERNAL          = "internal";
    String JETSTREAM         = "jetstream";
    String KEEP              = "keep";
    String LAG               = "lag";
    String LAME_DUCK_MODE    = "ldm";
    String LAST_ACTIVE       = "last_active";
    String LAST_BY_SUBJECT   = "last_by_subj";
    String LAST_ERROR        = "last_error";
    String LAST_SEQ          = "last_seq";
    String LAST_TS           = "last_ts";
    String LEADER            = "leader";
    String LIMIT             = "limit";
    String LIMITS            = "limits";
    String LINK              = "link";
    String LOST              = "lost";
    String MAX_ACK_PENDING   = "max_ack_pending";
    String MAX_AGE           = "max_age";
    String MAX_BATCH         = "max_batch";
    String MAX_BYTES         = "max_bytes";
    String MAX_BYTES_REQUIRED= "max_bytes_required";
    String MAX_CONSUMERS     = "max_consumers";
    String MAX_CHUNK_SIZE    = "max_chunk_size";
    String MAX_DELIVER       = "max_deliver";
    String MAX_EXPIRES       = "max_expires";
    String MAX_MEMORY        = "max_memory";
    String MAX_MSG_SIZE      = "max_msg_size";
    String MAX_MSGS          = "max_msgs";
    String MAX_MSGS_PER_SUB  = "max_msgs_per_subject";
    String MAX_PAYLOAD       = "max_payload";
    String MAX_STORAGE       = "max_storage";
    String MAX_STREAMS       = "max_streams";
    String MAX_WAITING       = "max_waiting"; // this is correct! the meaning name is different than the field name
    String MEM_STORAGE       = "mem_storage";
    String MEMORY            = "memory";
    String MEMORY_MAX_STREAM_BYTES = "memory_max_stream_bytes";
    String MESSAGE           = "message";
    String MESSAGES          = "messages";
    String METADATA          = "metadata";
    String MTIME             = "mtime";
    String MIRROR            = "mirror";
    String MSGS              = "msgs";
    String NAME              = "name";
    String NEXT_BY_SUBJECT   = "next_by_subj";
    String NO_ACK            = "no_ack";
    String NO_ERASE          = "no_erase";
    String NO_WAIT           = "no_wait";
    String NONCE             = "nonce";
    String NUID              = "nuid";
    String NUM_ACK_PENDING   = "num_ack_pending";
    String NUM_DELETED       = "num_deleted";
    String NUM_ERRORS        = "num_errors";
    String NUM_PENDING       = "num_pending";
    String NUM_REDELIVERED   = "num_redelivered";
    String NUM_REPLICAS      = "num_replicas";
    String NUM_REQUESTS      = "num_requests";
    String NUM_SUBJECTS      = "num_subjects";
    String NUM_WAITING       = "num_waiting";
    String OFFLINE           = "offline";
    String OFFSET            = "offset";
    String OPT_START_SEQ     = "opt_start_seq";
    String OPT_START_TIME    = "opt_start_time";
    String OPTIONS           = "options";
    String PLACEMENT         = "placement";
    String PORT              = "port";
    String PROCESSING_TIME   = "processing_time";
    String PROTO             = "proto";
    String PURGED            = "purged";
    String PUSH_BOUND        = "push_bound";
    String RATE_LIMIT_BPS    = "rate_limit_bps";
    String REPLAY_POLICY     = "replay_policy";
    String REPLICA           = "replica";
    String REPLICAS          = "replicas";
    String REPUBLISH         = "republish";
    String REQUEST           = "request";
    String RESPONSE          = "response";
    String RETENTION         = "retention";
    String SAMPLE_FREQ       = "sample_freq";
    String SCHEMA            = "schema";
    String SEALED            = "sealed";
    String SEQ               = "seq";
    String SERVER_ID         = "server_id";
    String SERVER_NAME       = "server_name";
    String SIZE              = "size";
    String SOURCE            = "source";
    String SOURCES           = "sources";
    String SRC               = "src";
    String STARTED           = "started";
    String STATE             = "state";
    String STATS             = "stats";
    String STORAGE           = "storage";
    String STORAGE_MAX_STREAM_BYTES = "storage_max_stream_bytes";
    String STREAM_NAME       = "stream_name";
    String STREAM_SEQ        = "stream_seq";
    String STREAM            = "stream";
    String STREAMS           = "streams";
    String SUBJECT           = "subject";
    String SUBJECTS          = "subjects";
    String SUBJECTS_FILTER   = "subjects_filter";
    String SUCCESS           = "success";
    String TAGS              = "tags";
    String TEMPLATE_OWNER    = "template_owner";
    String TIERS             = "tiers";
    String TIME              = "time";
    String TIMESTAMP         = "ts";
    String TLS               = "tls_required";
    String TOTAL             = "total";
    String TYPE              = "type";
    String VERSION           = "version";
}
