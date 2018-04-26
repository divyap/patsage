package com.patsage.microservices.usptoapp;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.patsage.MYSQLConnector;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author dprakash
 * Keywords extractor functionality handler
 */
class KeywordsExtractor {

	// Define the logger object for this class
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/**
     * Get list of keywords with stem form, frequency rank, and terms dictionary
     *
     * @param fullText
     * @return List<CardKeyword>, which contains keywords cards
     * @throws IOException
     */
    static List<Keyword> getKeywordsList(String fullText) throws IOException {

        TokenStream tokenStream = null;

        try {
            // treat the dashed words, don't let separate them during the processing
            fullText = fullText.replaceAll("-+", "-0");

            // replace any punctuation char but apostrophes and dashes with a space
            fullText = fullText.replaceAll("[\\p{Punct}&&[^'-]]+", " ");

            // replace most common English contractions
            fullText = fullText.replaceAll("(?:'(?:[tdsm]|[vr]e|ll))+\\b", "");

            StandardTokenizer stdToken = new StandardTokenizer();
            stdToken.setReader(new StringReader(fullText));

            tokenStream = new StopFilter(new ASCIIFoldingFilter(new ClassicFilter(new LowerCaseFilter(stdToken))), EnglishAnalyzer.getDefaultStopSet());
            tokenStream.reset();

            List<Keyword> cardKeywords = new LinkedList<>();

            CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);

            while (tokenStream.incrementToken()) {

                String term = token.toString();
                String stem = getStemForm(term);

                if (stem != null) {
                    Keyword cardKeyword = find(cardKeywords, new Keyword(stem.replaceAll("-0", "-")));
                    // treat the dashed words back, let look them pretty
                    cardKeyword.add(term.replaceAll("-0", "-"));
                }
            }

            // reverse sort by frequency
            Collections.sort(cardKeywords);

            return cardKeywords;
        } finally {
            if (tokenStream != null) {
                try {
                    tokenStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get stem form of the term
     *
     * @param term
     * @return String, which contains the stemmed form of the term
     * @throws IOException
     */
    private static String getStemForm(String term) throws IOException {

        TokenStream tokenStream = null;

        try {
            StandardTokenizer stdToken = new StandardTokenizer();
            stdToken.setReader(new StringReader(term));

            tokenStream = new PorterStemFilter(stdToken);
            tokenStream.reset();

            // eliminate duplicate tokens by adding them to a set
            Set<String> stems = new HashSet<>();

            CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);

            while (tokenStream.incrementToken()) {
                stems.add(token.toString());
            }

            // if stem form was not found or more than 2 stems have been found, return null
            if (stems.size() != 1) {
                return null;
            }

            String stem = stems.iterator().next();

            // if the stem form has non-alphanumerical chars, return null
            if (!stem.matches("[a-zA-Z0-9-]+")) {
                return null;
            }

            return stem;
        } finally {
            if (tokenStream != null) {
                try {
                    tokenStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Find sample in collection
     *
     * @param collection
     * @param sample
     * @param <T>
     * @return <T> T, which contains the found object within collection if exists, otherwise the initially searched object
     */
    private static <T> T find(Collection<T> collection, T sample) {

        for (T element : collection) {
            if (element.equals(sample)) {
                return element;
            }
        }

        collection.add(sample);

        return sample;
    }
    
    /*
     *  Method to extract keywords from Patents in database
     *  
     */
    public void extractPatentKeywords () {
 		MYSQLConnector mysql = new MYSQLConnector();
  		Connection conn = mysql.getmysqlConn();
  		PreparedStatement preparedStmt = null;
  		String sql = null;
		ResultSet rs = null;
		try {
  			// Fetch all searchkeywords rows
			sql = "Select searchId, patentnumber, patenttitle, patentabstract, topclaims from patsage.ps_patent";
  			logger.debug(sql);
  	  		// create the mysql insert preparedstatement
  	    	preparedStmt = conn.prepareStatement(sql);
  	    	rs = preparedStmt.executeQuery();
  	  		 if (rs != null ) {
  	 	    	while(rs.next()) {
  	 	    		// map to store keywords
  	 	    		Map patentKeywords = new HashMap();
  	 	    		int searchId = rs.getInt("searchId");
  	 	    		logger.debug("search ID ==>" + searchId);
  	 	    		String patentNum = rs.getString("patentnumber");
  	 	    		StringBuffer str = new StringBuffer();
  	 	    		str.append(rs.getString("patenttitle"));
  	 	    		str.append(rs.getString("patentabstract"));
  	 	    		str.append(rs.getString("topclaims"));
  	 	    		
	  	 	     	try {
	  	 				List<Keyword> keyList = getKeywordsList(str.toString());
	  	 				System.out.println("count of keywords =>" + keyList.size());
	  	 				for(Keyword key : keyList) {
	  	 					System.out.println("keyword =>" + key.getTerms().toString());
	  	 					System.out.println("frequency =>" + key.getFrequency());
	  	 					Connection insConn = mysql.getmysqlConn();
	  	 					String insertsql = "insert into patsage.ps_patent_keyword (search_id, patentnumber, keyword, frequency)"
		 							+ "VALUES (?, ?, ? ,? )";
	  	  	 	    		PreparedStatement insertStmt = insConn.prepareStatement(insertsql);
	  	 					try {
		  	 					insertStmt.setInt (1, searchId);
		  	 					insertStmt.setString (2, patentNum);
		  	 					insertStmt.setString (3, key.getTerms().toString());
		  	 					insertStmt.setInt (4, key.getFrequency());
		  	 					
		  	 					// execute the preparedstatement
		  	 					insertStmt.execute();
		  	 					
		  	 				} catch (Exception ex) {
		  	 					System.err.println("Got an exception!");
		  	 					System.err.println(ex.getMessage());
		  	 				} finally {
		  	 					if (insertStmt != null) {
		  	 						try {
		  	 							insertStmt.close();
		 	 			  		    } catch (SQLException sqlEx) { } // ignore
		  	 			  		    insertStmt = null;
		  	 			  		    
		  	 					}
		  	 			  		if (insConn != null) {
		  	 			  		    try {
		  	 			  		    	insConn.close();
		  	 			  		    } catch (SQLException sqlEx) { } // ignore
		  	 			  		        
		  	 			  		    insConn = null;
		  	 			  		}
		  	 				}
	  	 				} //end of for loop
	  	 			} catch (IOException e) {
	  	 				// TODO Auto-generated catch block
	  	 				e.printStackTrace();
	  	 			}
  	 	    	} // end of while loop
  	  		 }else {
  	  			logger.debug("Result set from ps_patent is NULL...");
  	  		 }
  		} catch (SQLException ex){
  		    // handle any errors
  		    System.err.println("SQLException: " + ex.getMessage());
  		    System.err.println("SQLState: " + ex.getSQLState());
  		    System.err.println("VendorError: " + ex.getErrorCode());
  		} finally {
  		    if (rs != null) {
  		        try {
  		            rs.close();
  		        } catch (SQLException sqlEx) { } // ignore
  		        rs = null;
  		    }
  		    if (preparedStmt != null) {
  		        try {
  		        	preparedStmt.close();
  		        } catch (SQLException sqlEx) { } // ignore
  		      preparedStmt = null;
  		    }
  		    if (conn != null) {
  		        try {
  		            conn.close();
  		        } catch (SQLException sqlEx) { } // ignore
  		        conn = null;
  		    }
  		}

    }
    
    public static void main(String args[]) {
    	
    	/*
    	String text = "Devices, systems and methods are disclosed for the ablation of tissue and treatment of cardiac arrhythmia. An ablation system includes an ablation catheter that has an array of ablation elements and a location element, an esophageal probe also including a location element, and an interface unit that provides energy to the ablation catheter. The distance between the location elements, determined by calculating means of the system, can be used by the system to set or modify one or more system parameters.";
    	
    	try {
			List<Keyword> keyList = getKeywordsList(text);
			System.out.println("count of keywords =>" + keyList.size());
			for(Keyword key : keyList) {
				System.out.println("keyword =>" + key.getTerms().toString());
				System.out.println("frequency =>" + key.getFrequency());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
    	
    	KeywordsExtractor keyExtract = new KeywordsExtractor();
    	keyExtract.extractPatentKeywords();
    	
    }
}
