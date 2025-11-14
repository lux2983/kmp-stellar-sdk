// Copyright 2025 Soneso. All rights reserved.
// Use of this source code is governed by a license that can be
// found in the LICENSE file.

package com.soneso.stellar.sdk.sep.sep01

/**
 * Simple TOML parser specifically designed for stellar.toml files.
 * Supports basic TOML features needed for SEP-0001.
 */
internal object TomlParser {

    fun parse(content: String): Map<String, Any?> {
        val lines = content.lines()
        val result = mutableMapOf<String, Any?>()
        var currentTable: String? = null
        var currentArrayTable: String? = null
        val arrayTables = mutableMapOf<String, MutableList<MutableMap<String, Any?>>>()
        var currentArrayTableData: MutableMap<String, Any?>? = null

        var i = 0
        while (i < lines.size) {
            val trimmed = lines[i].trim()

            // Skip empty lines and comments
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                i++
                continue
            }

            when {
                // Array of tables [[TABLE]]
                trimmed.startsWith("[[") && trimmed.endsWith("]]") -> {
                    val tableName = trimmed.substring(2, trimmed.length - 2).trim()
                    currentArrayTable = tableName
                    currentTable = null

                    // Create new entry in array of tables
                    if (!arrayTables.containsKey(tableName)) {
                        arrayTables[tableName] = mutableListOf()
                    }
                    currentArrayTableData = mutableMapOf()
                    arrayTables[tableName]!!.add(currentArrayTableData!!)
                    i++
                }
                // Regular table [TABLE]
                trimmed.startsWith("[") && trimmed.endsWith("]") -> {
                    val tableName = trimmed.substring(1, trimmed.length - 1).trim()
                    currentTable = tableName
                    currentArrayTable = null
                    currentArrayTableData = null
                    i++
                }
                // Key-value pair
                trimmed.contains("=") -> {
                    val parts = trimmed.split("=", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        var valueStr = parts[1].trim()

                        // Handle multi-line arrays
                        if (valueStr.startsWith("[") && !valueStr.endsWith("]")) {
                            // Multi-line array - collect all lines until ]
                            val arrayLines = mutableListOf(valueStr)
                            i++
                            while (i < lines.size) {
                                val nextLine = lines[i].trim()
                                arrayLines.add(nextLine)
                                if (nextLine.endsWith("]")) {
                                    break
                                }
                                i++
                            }
                            valueStr = arrayLines.joinToString(" ")
                        }

                        val value = parseValue(valueStr)

                        when {
                            currentArrayTableData != null -> {
                                // In array of tables
                                currentArrayTableData!![key] = value
                            }
                            currentTable != null -> {
                                // In regular table
                                val tableData = result[currentTable] as? MutableMap<String, Any?>
                                    ?: mutableMapOf<String, Any?>().also { result[currentTable!!] = it }
                                tableData[key] = value
                            }
                            else -> {
                                // Root level
                                result[key] = value
                            }
                        }
                    }
                    i++
                }
                else -> {
                    i++
                }
            }
        }

        // Add array tables to result
        arrayTables.forEach { (name, list) ->
            result[name] = list
        }

        return result
    }

    private fun parseValue(value: String): Any? {
        return when {
            // String
            value.startsWith("\"") && value.endsWith("\"") -> {
                value.substring(1, value.length - 1)
            }
            // Array
            value.startsWith("[") && value.endsWith("]") -> {
                parseArray(value)
            }
            // Boolean
            value == "true" -> true
            value == "false" -> false
            // Integer
            value.toLongOrNull() != null -> value.toLong()
            // Double
            value.toDoubleOrNull() != null -> value.toDouble()
            // Default to string
            else -> value
        }
    }

    private fun parseArray(arrayStr: String): List<Any?> {
        val content = arrayStr.substring(1, arrayStr.length - 1).trim()
        if (content.isEmpty()) {
            return emptyList()
        }

        val elements = mutableListOf<Any?>()
        var current = StringBuilder()
        var inString = false
        var depth = 0

        for (char in content) {
            when {
                char == '"' -> {
                    inString = !inString
                    current.append(char)
                }
                char == '[' && !inString -> {
                    depth++
                    current.append(char)
                }
                char == ']' && !inString -> {
                    depth--
                    current.append(char)
                }
                char == ',' && !inString && depth == 0 -> {
                    val element = current.toString().trim()
                    if (element.isNotEmpty()) {
                        elements.add(parseValue(element))
                    }
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }

        // Add the last element
        val element = current.toString().trim()
        if (element.isNotEmpty()) {
            elements.add(parseValue(element))
        }

        return elements
    }
}
