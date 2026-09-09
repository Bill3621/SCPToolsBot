/*
 * Copyright (c) 2024 Vxrpenter and the SCPToolsBot Contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain a copy of the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package dev.vxrp.configuration.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TranslationApplicationReview(
    @JsonProperty("TEXT_QUEUE_TITLE") String textQueueTitle,
    @JsonProperty("TEXT_QUEUE_BODY") String textQueueBody,
    @JsonProperty("TEXT_QUEUE_EMPTY") String textQueueEmpty,
    @JsonProperty("TEXT_QUEUE_ERROR") String textQueueError,
    @JsonProperty("TEXT_QUEUE_LOADING") String textQueueLoading,
    @JsonProperty("TEXT_QUEUE_FILTER_PLACEHOLDER") String textQueueFilterPlaceholder,
    @JsonProperty("TEXT_QUEUE_FILTER_OPEN") String textQueueFilterOpen,
    @JsonProperty("TEXT_QUEUE_FILTER_READY") String textQueueFilterReady,
    @JsonProperty("TEXT_QUEUE_FILTER_CLOSED") String textQueueFilterClosed,
    @JsonProperty("TEXT_QUEUE_FILTER_ALL") String textQueueFilterAll,
    @JsonProperty("TEXT_QUEUE_PAGE") String textQueuePage,
    @JsonProperty("TEXT_QUEUE_PREVIOUS") String textQueuePrevious,
    @JsonProperty("TEXT_QUEUE_NEXT") String textQueueNext,
    @JsonProperty("TEXT_QUEUE_REFRESH") String textQueueRefresh,
    @JsonProperty("TEXT_QUEUE_REVIEW") String textQueueReview,
    @JsonProperty("TEXT_REVIEW_TITLE") String textReviewTitle,
    @JsonProperty("TEXT_REVIEW_APPLICANT") String textReviewApplicant,
    @JsonProperty("TEXT_REVIEW_POSITION") String textReviewPosition,
    @JsonProperty("TEXT_REVIEW_SUBMITTED") String textReviewSubmitted,
    @JsonProperty("TEXT_REVIEW_ANSWERS") String textReviewAnswers,
    @JsonProperty("TEXT_REVIEW_NO_ANSWERS") String textReviewNoAnswers,
    @JsonProperty("TEXT_REVIEW_TALLY") String textReviewTally,
    @JsonProperty("TEXT_REVIEW_QUORUM") String textReviewQuorum,
    @JsonProperty("TEXT_REVIEW_QUORUM_REACHED") String textReviewQuorumReached,
    @JsonProperty("TEXT_REVIEW_QUORUM_PENDING") String textReviewQuorumPending,
    @JsonProperty("TEXT_REVIEW_YOUR_VOTE") String textReviewYourVote,
    @JsonProperty("TEXT_REVIEW_NOT_VOTED") String textReviewNotVoted,
    @JsonProperty("TEXT_REVIEW_OPEN_TICKET") String textReviewOpenTicket,
    @JsonProperty("TEXT_VOTE_PLACEHOLDER") String textVotePlaceholder,
    @JsonProperty("TEXT_VOTE_APPROVE") String textVoteApprove,
    @JsonProperty("TEXT_VOTE_DENY") String textVoteDeny,
    @JsonProperty("TEXT_VOTE_ABSTAIN") String textVoteAbstain,
    @JsonProperty("TEXT_VOTE_REMOVE") String textVoteRemove,
    @JsonProperty("TEXT_VOTE_RECORDED") String textVoteRecorded,
    @JsonProperty("TEXT_VOTE_CHANGED") String textVoteChanged,
    @JsonProperty("TEXT_VOTE_REMOVED") String textVoteRemoved,
    @JsonProperty("TEXT_VOTE_NOT_ALLOWED") String textVoteNotAllowed,
    @JsonProperty("TEXT_VOTE_CLOSED") String textVoteClosed,
    @JsonProperty("TEXT_CLOSE_BUTTON") String textCloseButton,
    @JsonProperty("TEXT_CLOSE_CONFIRM_TITLE") String textCloseConfirmTitle,
    @JsonProperty("TEXT_CLOSE_CONFIRM_BODY") String textCloseConfirmBody,
    @JsonProperty("TEXT_CLOSE_APPROVE") String textCloseApprove,
    @JsonProperty("TEXT_CLOSE_DENY") String textCloseDeny,
    @JsonProperty("TEXT_CLOSE_CANCEL") String textCloseCancel,
    @JsonProperty("TEXT_CLOSE_REASON_TITLE") String textCloseReasonTitle,
    @JsonProperty("TEXT_CLOSE_REASON_PLACEHOLDER") String textCloseReasonPlaceholder,
    @JsonProperty("TEXT_CLOSE_PERMISSION") String textClosePermission,
    @JsonProperty("TEXT_CLOSE_QUORUM") String textCloseQuorum,
    @JsonProperty("TEXT_CLOSE_TIE") String textCloseTie,
    @JsonProperty("TEXT_CLOSE_CONFIG") String textCloseConfig,
    @JsonProperty("TEXT_CLOSE_ERROR") String textCloseError,
    @JsonProperty("TEXT_CLOSE_SUCCESS") String textCloseSuccess,
    @JsonProperty("TEXT_CLOSE_ALREADY_CLOSED") String textCloseAlreadyClosed,
    @JsonProperty("TEXT_HISTORY_TITLE") String textHistoryTitle,
    @JsonProperty("TEXT_HISTORY_EMPTY") String textHistoryEmpty,
    @JsonProperty("TEXT_HISTORY_ENTRY") String textHistoryEntry,
    @JsonProperty("TEXT_HISTORY_PREVIOUS") String textHistoryPrevious,
    @JsonProperty("TEXT_HISTORY_NEXT") String textHistoryNext,
    @JsonProperty("TEXT_HISTORY_OMITTED") String textHistoryOmitted,
    @JsonProperty("TEXT_APPLICANT_APPROVED_TITLE") String textApplicantApprovedTitle,
    @JsonProperty("TEXT_APPLICANT_APPROVED_BODY") String textApplicantApprovedBody,
    @JsonProperty("TEXT_APPLICANT_DENIED_TITLE") String textApplicantDeniedTitle,
    @JsonProperty("TEXT_APPLICANT_DENIED_BODY") String textApplicantDeniedBody
) {}
