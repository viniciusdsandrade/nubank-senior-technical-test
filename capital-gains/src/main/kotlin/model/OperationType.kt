package model

import com.fasterxml.jackson.annotation.JsonProperty

enum class OperationType {
    @JsonProperty("buy")
    BUY,

    @JsonProperty("sell")
    SELL
}