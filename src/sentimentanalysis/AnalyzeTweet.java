/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sentimentanalysis;


import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static sentimentanalysis.Utilities.AnalyzeText;
import static sentimentanalysis.Utilities.CalculateScore;
import static sentimentanalysis.Utilities.ClassifyScore;
import static sentimentanalysis.Utilities.accuracy;
import static sentimentanalysis.Utilities.paging;
import static sentimentanalysis.Utilities.status;
import static sentimentanalysis.Utilities.twitter;
import static sentimentanalysis.Utilities.termIds;
import static sentimentanalysis.Utilities.tweetScore;
import static sentimentanalysis.Utilities.classification;
import static sentimentanalysis.Utilities.negScore;
import static sentimentanalysis.Utilities.partOfSpeech;
import static sentimentanalysis.Utilities.posScore;
import static sentimentanalysis.Utilities.termDictionary;
import twitter4j.Status;

import twitter4j.TwitterException;

/**
 *
 * @author Chucho
 */
public class AnalyzeTweet extends javax.swing.JFrame {

    /**
     * Creates new form LastTweet
     */
    public AnalyzeTweet() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtResultPanel = new javax.swing.JScrollPane();
        txtResult = new javax.swing.JTextArea();
        btnAnalyze = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        lblTweets = new javax.swing.JLabel();
        txtTweets = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtResult.setEditable(false);
        txtResult.setColumns(20);
        txtResult.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtResult.setRows(5);
        txtResultPanel.setViewportView(txtResult);

        btnAnalyze.setText("Analyze");
        btnAnalyze.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnalyzeActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnBack.setText("Back");
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        lblTweets.setText("Number of tweets");

        txtTweets.setText("1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtResultPanel)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnBack)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 359, Short.MAX_VALUE)
                        .addComponent(lblTweets)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtTweets, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 554, Short.MAX_VALUE)
                        .addComponent(btnAnalyze)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtResultPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefresh)
                    .addComponent(btnBack)
                    .addComponent(lblTweets)
                    .addComponent(txtTweets, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAnalyze)
                .addGap(42, 42, 42))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        try {
            paging.setCount(Integer.parseInt(txtTweets.getText()));
            status = Utilities.twitter.getHomeTimeline(paging);
            txtResult.setText("");
            int i=1;
            for (Status st : status){
                txtResult.append(i+":"+st.getCreatedAt()+" : "+st.getUser().getName()+" - "+ st.getText()+"\n");
                i++;
            }
        } catch (TwitterException ex) {
            txtResult.setText("Please wait "+ex.getRateLimitStatus().getSecondsUntilReset()+" seconds\n");
            Logger.getLogger(AnalyzeTweet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        Options functions=new Options();
        this.setVisible(false);
        functions.setVisible(true);
    }//GEN-LAST:event_btnBackActionPerformed

    private void btnAnalyzeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnalyzeActionPerformed
        
        try {
            long startTime=System.currentTimeMillis();
            paging.setCount(Integer.parseInt(txtTweets.getText()));
            //System.out.println(paging.getCount());
            status = twitter.getHomeTimeline(paging);
            //txtResult.setText(paging+status.toString());
            float totalScore=0;
            float totalAccuracy=0;
            for (Status st : status){
                //System.out.println(st);
                termIds=AnalyzeText(st.getText());
                //System.out.println(termIds);
                //termIds=AnalyzeTweets(status);
                int i=1;
                for (List<Integer> term : termIds)
                {
                    txtResult.append("\n"+String.valueOf(i)+".");
                    for(Integer id : term){
                        txtResult.append("Term "+termDictionary.get(id)+"("+partOfSpeech.get(id)+")" +" found with id '"+id.toString()+"'");
                        txtResult.append("\n Positive score : "+posScore.get(id));
                        txtResult.append("\n Negative score : "+negScore.get(id));
                        txtResult.append("\n Objectivity : "+String.valueOf(1-(posScore.get(id)+negScore.get(id)))+"\n");
                    }
                    i++;
                }
                tweetScore=CalculateScore(termIds);
                String result=String.valueOf(tweetScore);
                classification=ClassifyScore(tweetScore);
                txtResult.append("\n\n Tweet Score : "+result);
                txtResult.append("\n Accuracy : "+accuracy*100+"%");
                txtResult.append("\n Classified as : "+classification);
                txtResult.append("\n---------------------------------------------------------------\n");

                totalScore=totalScore+tweetScore;
                totalAccuracy=totalAccuracy+accuracy;
            }

            totalScore=totalScore/Integer.parseInt(txtTweets.getText());
            classification=ClassifyScore(totalScore);
            totalAccuracy=totalAccuracy/Integer.parseInt(txtTweets.getText());
            txtResult.append("Total score : "+totalScore);
            txtResult.append("\nTotal Accuracy : "+totalAccuracy*100+"%");
            txtResult.append("\nClassification : "+classification);
            long endTime=System.currentTimeMillis();
            long totalTimeSec=(endTime-startTime)/1000;
            txtResult.append("\nTime : "+totalTimeSec+" seconds.");
        } catch (TwitterException ex) {
            
            txtResult.setText("Please wait "+ex.getRateLimitStatus().getSecondsUntilReset()+" seconds\n");
            Logger.getLogger(AnalyzeTweet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_btnAnalyzeActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AnalyzeTweet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AnalyzeTweet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AnalyzeTweet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AnalyzeTweet.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AnalyzeTweet().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnalyze;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JLabel lblTweets;
    private javax.swing.JTextArea txtResult;
    private javax.swing.JScrollPane txtResultPanel;
    private javax.swing.JTextField txtTweets;
    // End of variables declaration//GEN-END:variables
}
