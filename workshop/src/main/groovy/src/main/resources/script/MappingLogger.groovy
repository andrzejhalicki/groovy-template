/*
 * Copyright (c) 2021 Miele & Cie. KG - All rights reserved.
 */

package src.main.resources.script

import com.sap.gateway.ip.core.customdev.util.Message

class MappingLogger {
    final List entries
    final String logLevel

    MappingLogger() {
        this.entries = []
        this.logLevel = 'INFO'
    }

    MappingLogger(Message message) {
        this.entries = []
        def mplConfig = message.getProperty('SAP_MessageProcessingLogConfiguration')
        this.logLevel = (mplConfig?.getLogLevel() ?: 'INFO') as String
    }

    void log(String entry) {
        this.entries.add(entry)
    }

    void info(String entry) {
        this.entries.add("[INFO] ${entry}")
    }

    void warn(String entry) {
        this.entries.add("[WARNING] ${entry}")
    }

    void debug(String entry) {
        if (this.logLevel == 'DEBUG' || this.logLevel == 'TRACE') {
            this.entries.add("[DEBUG] ${entry}")
        }
    }
}
