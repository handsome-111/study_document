package stu.wl.twitter.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.Tokenizer;

public class MyIKAnalyzer extends Analyzer {
    private boolean useSmart;
 
    public boolean useSmart() {
        return useSmart;
    }
    public void setUseSmart(boolean useSmart) {
        this.useSmart = useSmart;
    }
    public MyIKAnalyzer() {
        this(false);
    }
    public MyIKAnalyzer(boolean useSmart) {
        super();
        this.useSmart = useSmart;
    }

    @Override

    protected TokenStreamComponents createComponents(String fieldName) {

        Tokenizer _IKTokenizer = new MyIKTokenizer(this.useSmart());

        return new TokenStreamComponents(_IKTokenizer);

    }

}
