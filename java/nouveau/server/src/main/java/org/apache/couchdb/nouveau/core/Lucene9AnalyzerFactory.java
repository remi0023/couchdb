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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.couchdb.nouveau.api.IndexDefinition;

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
import l9x.org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
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

public final class Lucene9AnalyzerFactory {

    public Lucene9AnalyzerFactory() {
    }

    public Analyzer fromDefinition(final IndexDefinition indexDefinition) {
        final Analyzer defaultAnalyzer = newAnalyzer(indexDefinition.getDefaultAnalyzer());
        if (!indexDefinition.hasFieldAnalyzers()) {
            return defaultAnalyzer;
        }
        final Map<String, Analyzer> fieldAnalyzers = new HashMap<String, Analyzer>();
        for (Map.Entry<String, String> entry : indexDefinition.getFieldAnalyzers().entrySet()) {
            fieldAnalyzers.put(entry.getKey(), newAnalyzer(entry.getValue()));
        }
        return new PerFieldAnalyzerWrapper(defaultAnalyzer, fieldAnalyzers);
    }

    public Analyzer newAnalyzer(final String name) {
        switch(name) {
        case "keyword":
            return new KeywordAnalyzer();
        case "simple":
            return new SimpleAnalyzer();
        case "whitespace":
            return new WhitespaceAnalyzer();
        case "arabic":
            return new ArabicAnalyzer();
        case "bulgarian":
            return new BulgarianAnalyzer();
        case "catalan":
            return new CatalanAnalyzer();
        case "cjk":
            return new CJKAnalyzer();
        case "chinese":
            return new SmartChineseAnalyzer();
        case "czech":
            return new CzechAnalyzer();
        case "danish":
            return new DanishAnalyzer();
        case "german":
            return new GermanAnalyzer();
        case "english":
            return new EnglishAnalyzer();
        case "spanish":
            return new SpanishAnalyzer();
        case "basque":
            return new BasqueAnalyzer();
        case "persian":
            return new PersianAnalyzer();
        case "finnish":
            return new FinnishAnalyzer();
        case "french":
            return new FrenchAnalyzer();
        case "irish":
            return new IrishAnalyzer();
        case "galician":
            return new GalicianAnalyzer();
        case "hindi":
            return new HindiAnalyzer();
        case "hungarian":
            return new HungarianAnalyzer();
        case "armenian":
            return new ArmenianAnalyzer();
        case "indonesian":
            return new IndonesianAnalyzer();
        case "italian":
            return new ItalianAnalyzer();
        case "japanese":
            return new JapaneseAnalyzer();
        case "latvian":
            return new LatvianAnalyzer();
        case "dutch":
            return new DutchAnalyzer();
        case "norwegian":
            return new NorwegianAnalyzer();
        case "polish":
            return new PolishAnalyzer();
        case "portugese":
            return new PortugueseAnalyzer();
        case "romanian":
            return new RomanianAnalyzer();
        case "russian":
            return new RussianAnalyzer();
        case "classic":
            return new ClassicAnalyzer();
        case "standard":
            return new StandardAnalyzer();
        case "email":
            return new UAX29URLEmailAnalyzer();
        case "swedish":
            return new SwedishAnalyzer();
        case "thai":
            return new ThaiAnalyzer();
        case "turkish":
            return new TurkishAnalyzer();
        default:
            throw new WebApplicationException(name + " is not a valid analyzer name", Status.BAD_REQUEST);
        }
    }

}
