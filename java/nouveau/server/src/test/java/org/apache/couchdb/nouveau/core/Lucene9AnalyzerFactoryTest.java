//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.couchdb.nouveau.core;

import static org.assertj.core.api.Assertions.assertThat;

import l9x.org.apache.lucene.analysis.Analyzer;
import l9x.org.apache.lucene.analysis.ar.ArabicAnalyzer;
import l9x.org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import l9x.org.apache.lucene.analysis.ca.CatalanAnalyzer;
import l9x.org.apache.lucene.analysis.cjk.CJKAnalyzer;
import l9x.org.apache.lucene.analysis.classic.ClassicAnalyzer;
import l9x.org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import l9x.org.apache.lucene.analysis.core.KeywordAnalyzer;
import l9x.org.apache.lucene.analysis.core.SimpleAnalyzer;
import l9x.org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import l9x.org.apache.lucene.analysis.cz.CzechAnalyzer;
import l9x.org.apache.lucene.analysis.da.DanishAnalyzer;
import l9x.org.apache.lucene.analysis.de.GermanAnalyzer;
import l9x.org.apache.lucene.analysis.email.UAX29URLEmailAnalyzer;
import l9x.org.apache.lucene.analysis.en.EnglishAnalyzer;
import l9x.org.apache.lucene.analysis.es.SpanishAnalyzer;
import l9x.org.apache.lucene.analysis.eu.BasqueAnalyzer;
import l9x.org.apache.lucene.analysis.fa.PersianAnalyzer;
import l9x.org.apache.lucene.analysis.fi.FinnishAnalyzer;
import l9x.org.apache.lucene.analysis.fr.FrenchAnalyzer;
import l9x.org.apache.lucene.analysis.ga.IrishAnalyzer;
import l9x.org.apache.lucene.analysis.gl.GalicianAnalyzer;
import l9x.org.apache.lucene.analysis.hi.HindiAnalyzer;
import l9x.org.apache.lucene.analysis.hu.HungarianAnalyzer;
import l9x.org.apache.lucene.analysis.hy.ArmenianAnalyzer;
import l9x.org.apache.lucene.analysis.id.IndonesianAnalyzer;
import l9x.org.apache.lucene.analysis.it.ItalianAnalyzer;
import l9x.org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import l9x.org.apache.lucene.analysis.lv.LatvianAnalyzer;
import l9x.org.apache.lucene.analysis.nl.DutchAnalyzer;
import l9x.org.apache.lucene.analysis.no.NorwegianAnalyzer;
import l9x.org.apache.lucene.analysis.pl.PolishAnalyzer;
import l9x.org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import l9x.org.apache.lucene.analysis.ro.RomanianAnalyzer;
import l9x.org.apache.lucene.analysis.ru.RussianAnalyzer;
import l9x.org.apache.lucene.analysis.standard.StandardAnalyzer;
import l9x.org.apache.lucene.analysis.sv.SwedishAnalyzer;
import l9x.org.apache.lucene.analysis.th.ThaiAnalyzer;
import l9x.org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.junit.jupiter.api.Test;

public class Lucene9AnalyzerFactoryTest {

    @Test
    public void testkeyword() throws Exception {
        assertAnalyzer("keyword", KeywordAnalyzer.class);
    }

    @Test
    public void testsimple() throws Exception {
        assertAnalyzer("simple", SimpleAnalyzer.class);
    }

    @Test
    public void testwhitespace() throws Exception {
        assertAnalyzer("whitespace", WhitespaceAnalyzer.class);
    }

    @Test
    public void testarabic() throws Exception {
        assertAnalyzer("arabic", ArabicAnalyzer.class);
    }

    @Test
    public void testbulgarian() throws Exception {
        assertAnalyzer("bulgarian", BulgarianAnalyzer.class);
    }

    @Test
    public void testcatalan() throws Exception {
        assertAnalyzer("catalan", CatalanAnalyzer.class);
    }

    @Test
    public void testcjk() throws Exception {
        assertAnalyzer("cjk", CJKAnalyzer.class);
    }

    @Test
    public void testchinese() throws Exception {
        assertAnalyzer("chinese", SmartChineseAnalyzer.class);
    }

    @Test
    public void testczech() throws Exception {
        assertAnalyzer("czech", CzechAnalyzer.class);
    }

    @Test
    public void testdanish() throws Exception {
        assertAnalyzer("danish", DanishAnalyzer.class);
    }

    @Test
    public void testgerman() throws Exception {
        assertAnalyzer("german", GermanAnalyzer.class);
    }

    @Test
    public void testenglish() throws Exception {
        assertAnalyzer("english", EnglishAnalyzer.class);
    }

    @Test
    public void testspanish() throws Exception {
        assertAnalyzer("spanish", SpanishAnalyzer.class);
    }

    @Test
    public void testbasque() throws Exception {
        assertAnalyzer("basque", BasqueAnalyzer.class);
    }

    @Test
    public void testpersian() throws Exception {
        assertAnalyzer("persian", PersianAnalyzer.class);
    }

    @Test
    public void testfinnish() throws Exception {
        assertAnalyzer("finnish", FinnishAnalyzer.class);
    }

    @Test
    public void testfrench() throws Exception {
        assertAnalyzer("french", FrenchAnalyzer.class);
    }

    @Test
    public void testirish() throws Exception {
        assertAnalyzer("irish", IrishAnalyzer.class);
    }

    @Test
    public void testgalician() throws Exception {
        assertAnalyzer("galician", GalicianAnalyzer.class);
    }

    @Test
    public void testhindi() throws Exception {
        assertAnalyzer("hindi", HindiAnalyzer.class);
    }

    @Test
    public void testhungarian() throws Exception {
        assertAnalyzer("hungarian", HungarianAnalyzer.class);
    }

    @Test
    public void testarmenian() throws Exception {
        assertAnalyzer("armenian", ArmenianAnalyzer.class);
    }

    @Test
    public void testindonesian() throws Exception {
        assertAnalyzer("indonesian", IndonesianAnalyzer.class);
    }

    @Test
    public void testitalian() throws Exception {
        assertAnalyzer("italian", ItalianAnalyzer.class);
    }

    @Test
    public void testjapanese() throws Exception {
        assertAnalyzer("japanese", JapaneseAnalyzer.class);
    }

    @Test
    public void testlatvian() throws Exception {
        assertAnalyzer("latvian", LatvianAnalyzer.class);
    }

    @Test
    public void testdutch() throws Exception {
        assertAnalyzer("dutch", DutchAnalyzer.class);
    }

    @Test
    public void testnorwegian() throws Exception {
        assertAnalyzer("norwegian", NorwegianAnalyzer.class);
    }

    @Test
    public void testpolish() throws Exception {
        assertAnalyzer("polish", PolishAnalyzer.class);
    }

    @Test
    public void testportugese() throws Exception {
        assertAnalyzer("portugese", PortugueseAnalyzer.class);
    }

    @Test
    public void testromanian() throws Exception {
        assertAnalyzer("romanian", RomanianAnalyzer.class);
    }

    @Test
    public void testrussian() throws Exception {
        assertAnalyzer("russian", RussianAnalyzer.class);
    }

    @Test
    public void testclassic() throws Exception {
        assertAnalyzer("classic", ClassicAnalyzer.class);
    }

    @Test
    public void teststandard() throws Exception {
        assertAnalyzer("standard", StandardAnalyzer.class);
    }

    @Test
    public void testemail() throws Exception {
        assertAnalyzer("email", UAX29URLEmailAnalyzer.class);
    }

    @Test
    public void testswedish() throws Exception {
        assertAnalyzer("swedish", SwedishAnalyzer.class);
    }

    @Test
    public void testthai() throws Exception {
        assertAnalyzer("thai", ThaiAnalyzer.class);
    }

    @Test
    public void testturkish() throws Exception {
        assertAnalyzer("turkish", TurkishAnalyzer.class);
    }

    private void assertAnalyzer(final String name, final Class<? extends Analyzer> clazz) throws Exception {
        final Lucene9AnalyzerFactory factory = new Lucene9AnalyzerFactory();
        assertThat(factory.newAnalyzer(name)).isInstanceOf(clazz);
    }

}
