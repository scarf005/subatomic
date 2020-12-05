package subatomic
package search

import weaver.PureIOSuite
import weaver.SimpleMutableIOSuite

object IndexerTests extends SimpleMutableIOSuite {

  val content = Vector(
    "/"            -> "lorem ipsum dolor amet lorem",
    "/hello"       -> "lorem dolor",
    "/hello/world" -> "amet ipsum amet dolor"
  )

  val idx = Indexer.default[String, String](content).processAll(identity)

  pureTest("all documents have entry in the index") {
    expect(
      idx.documentsMapping.values.toSet == content
        .map(_._1)
        .toSet
    )
  }

  pureTest("all words have entry in the index") {
    val allWords = Set("lorem", "ipsum", "dolor", "amet")

    expect(
      idx.termMapping.keySet.map(_.value) == allWords
    )
  }

  pureTest("all terms have entries in the terms-to-documents mapping") {
    def getMapping(term: String) =
      idx.termsInDocuments(idx.termMapping(TermName(term)))

    expect.all(
      getMapping("lorem").nonEmpty,
      getMapping("ipsum").nonEmpty,
      getMapping("dolor").nonEmpty,
      getMapping("amet").nonEmpty
    )
  }

  pureTest("global term frequency is correctly calculated") {
    def getGlobalFrequency(term: String) =
      idx.termsInDocuments(idx.termMapping(TermName(term))).size

    expect.all(
      getGlobalFrequency("lorem") == 2,
      getGlobalFrequency("ipsum") == 2,
      getGlobalFrequency("dolor") == 3,
      getGlobalFrequency("amet") == 2
    )
  }

  pureTest("in document frequency is correctly calculated") {
    def getInDocumentFrequency(term: String, document: String) = {
      val termIdx = idx.termMapping(TermName(term))
      val docIdx  = idx.documentsMapping.map(_.swap).apply(document)

      idx.termsInDocuments(termIdx)(docIdx).value
    }

    expect.all(
      getInDocumentFrequency("lorem", "/") == 2,
      getInDocumentFrequency("ipsum", "/") == 1,
      getInDocumentFrequency("dolor", "/") == 1,
      getInDocumentFrequency("amet", "/") == 1,
      getInDocumentFrequency("lorem", "/hello") == 1,
      getInDocumentFrequency("dolor", "/hello") == 1,
      getInDocumentFrequency("amet", "/hello/world") == 2,
      getInDocumentFrequency("ipsum", "/hello/world") == 1,
      getInDocumentFrequency("dolor", "/hello/world") == 1
    )
  }

  pureTest("document index is correct") {
    def getDocumentTerms(id: String) = {
      val docIdx             = idx.documentsMapping.map(_.swap).apply(id)
      val reverseTermMapping = idx.termMapping.map(_.swap)
      idx.documentTerms(docIdx).map {
        case (termIdx, termFreq) =>
          reverseTermMapping(termIdx).value -> termFreq.value
      }
    }

    expect.all(
      getDocumentTerms("/") == Map(
        "lorem" -> 2,
        "ipsum" -> 1,
        "dolor" -> 1,
        "amet"  -> 1
      ),
      getDocumentTerms("/hello") == Map(
        "lorem" -> 1,
        "dolor" -> 1
      ),
      getDocumentTerms("/hello/world") == Map(
        "ipsum" -> 1,
        "dolor" -> 1,
        "amet"  -> 2
      )
    )
  }
}
