/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.cnl.bm;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.apache.commons.net.ntp.TimeStamp;

/**
 *
 * @author veri
 */
public class TimeTest {
    
    public void testik() {
        
        long flowStartNanoseconds = -3226406474260943772L;
        TimeStamp ntp = new TimeStamp(flowStartNanoseconds);
        System.out.println("Cas: " + ntp.toDateString());
        
    }

    public void test() {

        Date date = new TimeStamp(1).getDate();

        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(utcZone);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(1970, 0, 1, 0, 0, 0);
        long time = calendar.getTime().getTime();


//        System.out.println(date);

        long exportTime = 1334265812L;
        exportTime = System.currentTimeMillis() / 1000;    //!!!
        long millis = exportTime * 1000;

        Date d = new Date(millis);

        TimeStamp ntp = new TimeStamp(d); // cas exportu ako timestamp
        System.out.println("seconds = " + ntp.getSeconds());

//        long seconds = ntp.getSeconds();
//
//
        long offset = 3620000; // 3 a pol sekundy
//
//        long offsetSekundy = offset / 10 ^ 6;
//
//        long newOffset = offset - offsetSekundy; // tu by sme mali novu cast sekund
//
//
//        long sekunda = 10 ^ 6;
//
//        long novesekundy = seconds - offsetSekundy - 1;
////        long novemikrosekundy = sekunda - mikrosekundy;
//
//
//
//
//
//

        long newSeconds = ntp.getSeconds() - (offset / 1000000);             // sekundy z exportTime  - sekundy posunu
        long mikrosekundy = offset % 1000000;
        if (mikrosekundy > 1) {
            newSeconds -= 1;
        }
        System.out.println("mikrosekundy = " + mikrosekundy);


        long newMicroseconds = 1000000 - (offset % 1000000);                  // z tohto treba spravit zlomok

        System.out.println("newSeconds: " + newSeconds);
        System.out.println("newMicroseconds: " + newMicroseconds);

        long zlomok = (newMicroseconds / 10 ^ 9) * 2 ^ 32;

//        String fractionHex = Long.toHexString(zlomok);

//        String secondsHex = Long.toHexString(newSeconds);

//        System.out.println("fractionHex: " + fractionHex);
//        System.out.println("secondsHex: " + secondsHex);

        // vo vysledku by sme mali dostat normalny cas.

        StringBuffer buf = new StringBuffer();
        appendHexString(buf, newSeconds);
        buf.append('.');
        appendHexString(buf, zlomok);

        String ntpString = buf.toString();
        System.out.println(ntpString);


        TimeStamp t = new TimeStamp(ntpString);

        System.out.println("Datumik po prevode: " + t.getDate());





        // cast fraction bude pri export time vzdy 0, takze ten offset musime odpocitat





    }

    public static long decodeDeltaMicrosecondsTimestamp(long exportTimeInSeconds, long negativeOffsetInMicroseconds) {
        // vytvorime NTP obsahujuce sekundy od roku 1900 z exportneho casu, fraction je 0
        TimeStamp ntp = new TimeStamp(new Date(exportTimeInSeconds * 1000));
        long sekundy = ntp.getSeconds() - (negativeOffsetInMicroseconds / 1000000);

        BigInteger bigSecondsToMicroseconds = BigInteger.valueOf(ntp.getSeconds());
        bigSecondsToMicroseconds.multiply(BigInteger.valueOf(1000000)); // toto uz mame v mikrosekundach

        BigInteger milion = BigInteger.valueOf(1000000);

        BigInteger offset = BigInteger.valueOf(negativeOffsetInMicroseconds);

        // odcitame celkove offset of celkovych mikrosekund, podelime sekundami. Sekundy su v pole[0], mikrosekundy v pole[1]
        BigInteger[] pole = bigSecondsToMicroseconds.subtract(offset).divideAndRemainder(milion);

        sekundy = pole[0].longValue();

        long mikrosekundy = pole[1].longValue();



//        long ntpMicro = ntp.getSeconds() * 1000000; // toto je v mikrosekundach

//        long zvysok = negativeOffsetInMicroseconds % 1000000;

        // ak mame nenulovy negativny offset v mikrosekundach, tak odcitame zo sekund
//        sekundy = (zvysok > 0) ? sekundy - 1 : sekundy;

        long newFraction = mikrosekundy / 10 ^ 9 * 2 ^ 32;


        StringBuffer buf = new StringBuffer();
        appendHexString(buf, sekundy);
        buf.append('.');
        appendHexString(buf, newFraction);
        String ntpString = buf.toString();
        System.out.println(ntpString);
        TimeStamp t = new TimeStamp(ntpString);
        System.out.println("cas: " + t.getDate());
        return t.ntpValue();
    }

    public static long decodeDeltaMicroseconds(long exportTimeInSeconds, long negativeOffsetInMicroseconds) {
        TimeStamp ntp = new TimeStamp(new Date(exportTimeInSeconds * 1000));
        long sekundy = ntp.getSeconds() - (negativeOffsetInMicroseconds / 1000000);

        long zvysok = negativeOffsetInMicroseconds % 1000000;
        // ak mame nenulovy negativny offset v mikrosekundach, tak odcitame zo sekund
        sekundy = (zvysok > 0) ? sekundy - 1 : sekundy;

        long newMicroseconds = 1000000 - (negativeOffsetInMicroseconds % 1000000);                  // z tohto treba spravit zlomok
        long zlomok = newMicroseconds / 1000000000 * (2 ^ 32);

        StringBuffer buf = new StringBuffer();
        appendHexString(buf, sekundy);
        buf.append('.');
        appendHexString(buf, zlomok);

        String ntpString = buf.toString();
        System.out.println(ntpString);


        TimeStamp t = new TimeStamp(ntpString);
        System.out.println("cas: " + t.getDate());
        return t.ntpValue();
    }

    /***
     * Left-pad 8-character hex string with 0's
     *
     * @param buf - StringBuffer which is appended with leading 0's.
     * @param l - a long.
     */
    private static void appendHexString(StringBuffer buf, long l) {
        String s = Long.toHexString(l);
        for (int i = s.length(); i < 8; i++) {
            buf.append('0');
        }
        buf.append(s);
    }
}
