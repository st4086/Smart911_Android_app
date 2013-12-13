package wh.cmc.smart911beta13;

import com.musicg.api.DetectionApi;
import com.musicg.wave.WaveHeader;


public class MyDetect extends DetectionApi{
        
        public MyDetect(WaveHeader waveHeader) {
                super(waveHeader);
        }

        protected void init(){
            // settings for detecting a whistle
            minFrequency = 1000.0f;
            maxFrequency = Double.MAX_VALUE;
            
            // get the decay part of a clap
            minIntensity = 10000.0f;
            maxIntensity = 100000.0f;
            
            //key features
            minStandardDeviation = 0.0f;
            maxStandardDeviation = 0.05f;
            
            highPass = 100;
            lowPass = 10000;
            
            minNumZeroCross = 100;
            maxNumZeroCross = 500;
            
            numRobust = 4;
        }
                
        public boolean isWhistle(byte[] audioBytes){
                return isSpecificSound(audioBytes);
        }
}

