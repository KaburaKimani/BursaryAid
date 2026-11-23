package com.example.bursaryaid.feature.admin.logic

/** Field keys for the decision form's error map, as in Member 2's FormField object. */
object DecisionForm {
    const val FIELD_AMOUNT = "awardedAmountKes"
    const val FIELD_REASON = "decisionReason"

    /**
     * Reads a whole-shilling amount from text. Thousands separators ("40,000" or "40 000")
     * are accepted. Anything else returns null: letters, decimals, negatives, or a
     * number too big to store.
     */
    fun parseAmount(text: String): Long? {
        val cleaned = text.trim().replace(",", "").replace(" ", "")
        if (cleaned.isEmpty() || !cleaned.all { it in '0'..'9' }) return null
        return cleaned.toLongOrNull()
    }
}