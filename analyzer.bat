@echo off
java -Dpaoding.try.app=analyzer.bat -Dpaoding.try.cmd=analyzer.bat -cp .;paoding-analysis.jar;./lib/commons-logging.jar;./lib/lucene-core-2.2.0.jar;./lib/lucene-analyzers-2.2.0.jar;./lib/lucene-highlighter-2.2.0.jar net.paoding.analysis.analyzer.TryPaodingAnalyzer %1 %2 %3 %4 %5 %6 %7 %8 %9
