import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String sourceFile) {
    String currentSequence = "";
    char nextChar;
    In reader = new In(sourceFile);
    while ((!reader.isEmpty()) && (currentSequence.length() < windowLength)) {
        nextChar = reader.readChar();
        currentSequence += nextChar;
    }
    while (!reader.isEmpty()) {
        nextChar = reader.readChar();
        List charList = CharDataMap.get(currentSequence);
        if (charList == null) {
            charList = new List();
            CharDataMap.put(currentSequence, charList);
        }
        charList.update(nextChar);
        currentSequence += nextChar;
        currentSequence = currentSequence.substring(1, currentSequence.length());
    }
    for (List entry : CharDataMap.values())
        calculateProbabilities(entry);
}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	// Computes and sets the probabilities (p and cp fields) of all the
    // characters in the given list. */
    void calculateProbabilities(List charList) {               
        int totalCount = 0;
        for (int idx = 0; idx < charList.getSize(); ++idx) {
            totalCount += charList.get(idx).count;
        }
        for (int idx = 0; idx < charList.getSize(); ++idx) {
            charList.get(idx).p = charList.get(idx).count / (double)totalCount; 
            double prevCp = (idx > 0 ? charList.get(idx - 1).cp : 0);
            charList.get(idx).cp = charList.get(idx).p + prevCp; 
        }
    }

	char getRandomChar(List probs) {
		double random = randomGenerator.nextDouble();
		char charToReturn = ' ';
		for (int i = 0; i < probs.getSize(); ++i) {
			if (random < probs.get(i).cp) {
				charToReturn = probs.get(i).chr;
				break;
			}
		}
		return charToReturn;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength)
			return initialText;

		String generatedText = initialText;
		for (int i = 0; i < textLength; ++i) {
			String window = generatedText.substring(generatedText.length() - windowLength, generatedText.length());
			List probs = CharDataMap.get(window);
			if (probs == null)
				return generatedText;

			generatedText += getRandomChar(probs);
		}
		return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	/** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        int contextSize = Integer.parseInt(args[0]);
        String seedText = args[1];
        int targetLength = Integer.parseInt(args[2]);
        Boolean isRandomMode = args[3].equals("random");
        String corpusFile = args[4];
        LanguageModel textModel;
        if (isRandomMode)
            textModel = new LanguageModel(contextSize);
        else
            textModel = new LanguageModel(contextSize, 20);
        textModel.train(corpusFile);
        System.out.println(textModel.generate(seedText, targetLength));
    }
}
