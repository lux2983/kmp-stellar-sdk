package com.soneso.stellar.sdk.datalake

/**
 * Exception thrown when data lake operations fail.
 */
class DataLakeException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
