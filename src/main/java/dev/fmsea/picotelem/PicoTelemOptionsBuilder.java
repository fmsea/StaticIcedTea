package dev.fmsea.picotelem;

public class PicoTelemOptionsBuilder {
        private String analysisEventsFile = null;

        public PicoTelemOptionsBuilder() {
        }

        public PicoTelemOptionsBuilder withAnalysisEventsFile(String filename) {
            this.analysisEventsFile = filename;
            return this;
        }

        public PicoTelemOptions build() {
            return new PicoTelemOptions(this.analysisEventsFile);
        }
    }
