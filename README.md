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
 
    
