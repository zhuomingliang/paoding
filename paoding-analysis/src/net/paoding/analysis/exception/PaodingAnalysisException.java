package net.paoding.analysis.exception;

@SuppressWarnings("serial")
public class PaodingAnalysisException extends RuntimeException {

	public PaodingAnalysisException() {
		super();
	}

	public PaodingAnalysisException(String message, Throwable cause) {
		super(message, cause);
	}

	public PaodingAnalysisException(String message) {
		super(message);
	}

	public PaodingAnalysisException(Throwable cause) {
		super(cause);
	}

}
