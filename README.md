# Workflow TGDP

This repo was created in preparation of the second release of the TGDP ZuMult platform at UT Austin.
It rebundles the code for preparing the TGDP data from speechislands.org for inclusion in the platform.
The code takes care of:

## Conversion and Annotation
- Converting EAF to ISO/TEI conformant XML
- Tokenisation
- Language tagging
- Orthographic normalisation
- Normalisation
- Lemmatisation
- Part-of-Speech tagging (according to STTS 2.0 and Universal Dependencies)
- Phonetic annotation (using the G2P web service from BAS Munich)
- Speech rate annotation

## Indexing
  - Lucene index for MTAS (for query in ZuMult)
  - Indexing of the COMA file (for quicker access in ZuMult)
  - Stats for the COMA file (for quicker access in ZuMult)

## Usage

This Windows batch file (or a Linux equivalent) bundles all commands:

[ConvertAnnotateIndex](src/main/java/de/linguisticbits/workflow/ConvertAnnotateIndex.bat)

Additionally, a ZuMult configuration file has to be set on the system with suitable values as follows:

```
<configuration>
  <backend classPath="org.zumult.backend.implementations.COMAFileSystem">              
    <tree-tagger-directory>C:\Users\bernd\Dropbox\TreeTagger</tree-tagger-directory>         
    <tree-tagger-parameter-file-german>C:\linguisticbits_nb\2021-04-16_ParameterFile_ORIGINAL_ALL_FINAL.par</tree-tagger-parameter-file-german>       
    <tree-tagger-parameter-file-english>C:\linguisticbits_nb\english.par</tree-tagger-parameter-file-english>

    <!-- Phonetic lexicons, see section 1.9 -->
    <phonetic-lexicon-german>C:\linguisticbits_nb\Lexicon_German.xml</phonetic-lexicon-german>        
    <phonetic-lexicon-english>C:\linguisticbits_nb\Lexicon_English.xml</phonetic-lexicon-english>        
    <phonetic-lexicon-other>C:\linguisticbits_nb\Lexicon_Other.xml</phonetic-lexicon-other>
  </backend>
</configuration>
```

Setting a ZuMult configuration is done by
- Saving an XML file like the above in a suitable place in the system
- Specifying the path to that file in an environment variable `ZUMULT_CONFIG_PATH`

The TreeTagger binary must be downloaded from https://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/.
The two TreeTagger parameter files are part of this repository: [tagger package](src/main/java/de/linguisticbits/workflow/tagger)
The three phonetic lexicons are also part of this repository: [normalizer package](src/main/java/de/linguisticbits/workflow/normalizer)

In the batch file, you need to adapt the variables `WORKFLOW_JAR` and `LIB_DIRECTORY`

When calling the batch file, you need to specify four parameters:

- The path to the COMA file ([...]/TGDP.coma)
- The path to the MTAS configuration file - part of this repository: [MTAS config](src/main/java/de/linguisticbits/workflow/indexing/tgdp_mtas_config_SB.xml)
- The path of the directory to which the MTAS/Lucene index will be written
- The name of the MTAS/Lucene index (SB_TGDP)

  
  



 
    
