<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:tei="http://www.tei-c.org/ns/1.0"
    xmlns:exmaralda="http://www.exmaralda.org"
    exclude-result-prefixes="xs" version="2.0">
    
    <xsl:param name="PRIMARY_TIER_TYPE" select="'parent'"/>
    <xsl:param name="ORTH_TIER_TYPE" select="'orthT'"/>
    <xsl:param name="WORD_TIER_TYPE" select="'wordT'"/>
    <xsl:param name="TRANSCRIPT-ID"/>
    
    <xsl:variable name="COPY_TIME">
        <xsl:copy-of select="//TIME_ORDER"/>
    </xsl:variable>
    <xsl:function name="exmaralda:TIME_SLOT_POSITION">
        <xsl:param name="LOOKUP_ID"/>
        <xsl:value-of select="count($COPY_TIME/descendant::TIME_SLOT[@TIME_SLOT_ID=$LOOKUP_ID]/preceding-sibling::*)"/>
    </xsl:function>
    
    <xsl:template match="/">
        <tei:TEI>
            
            <tei:idno type="TGDP-ID"><xsl:value-of select="$TRANSCRIPT-ID"/></tei:idno>

            <tei:teiHeader>
                <tei:fileDesc>
                    <tei:titleStmt>
                        <tei:title><xsl:value-of select="base-uri()"/></tei:title>
                    </tei:titleStmt>

                    <!-- *********************************** -->
                    <!-- Distribution information, see 4.1.1 -->
                    <!-- *********************************** -->
                    <!-- <tei:publicationStmt>
                    </tei:publicationStmt> -->

                    <!-- ******************************** -->
                    <!-- Recording information, see 4.1.2 -->
                    <!-- ******************************** -->
                    <tei:sourceDesc>
                        <tei:recordingStmt>
                            <tei:recording type="audio">
                                <tei:media mimeType="audio/wav">
                                    <xsl:attribute name="url">
                                        <xsl:value-of select="tokenize(/ANNOTATION_DOCUMENT/HEADER[1]/MEDIA_DESCRIPTOR[1]/@MEDIA_URL, '/')[last()]"/>
                                    </xsl:attribute>
                                </tei:media>
                                
                            </tei:recording>
                        </tei:recordingStmt>
                    </tei:sourceDesc>
                </tei:fileDesc>

                <tei:profileDesc>

                    <!-- ********************************** -->
                    <!-- Participant information, see 4.2.1 -->
                    <!-- ********************************** -->
                    <tei:particDesc>
                        <xsl:for-each-group select="//TIER" group-by="@PARTICIPANT">
                        <!-- <xsl:for-each-group select="//TIER[not(@PARENT_REF)]" group-by="@TIER_ID"> -->
                            <tei:person>
                                <xsl:attribute name="xml:id" select="current-grouping-key()"/>
                                <xsl:attribute name="n" select="current-grouping-key()"/>
                                <tei:idno type="TGDP-ID"><xsl:value-of select="current-grouping-key()"/></tei:idno>
                                <tei:persName><tei:forename/><tei:abbr><xsl:value-of select="current-grouping-key()"/></tei:abbr></tei:persName>
                            </tei:person>
                        </xsl:for-each-group>
                    </tei:particDesc>
                    

                    <!-- ****************************** -->
                    <!-- Setting information, see 4.2.2 -->
                    <!-- ****************************** -->
                    <!-- <tei:settingDesc> -->
                        <!-- FILL ME IN -->
                    <!-- </tei:settingDesc> -->

                </tei:profileDesc>

                <!-- ****************************** -->
                <!-- Description of source, see 4.3 -->
                <!-- ****************************** -->
                <tei:encodingDesc>
                    <tei:appInfo>
                        <tei:application ident="ELAN" version="6.4">
                            <tei:label>EUDICO Linguistic Annotator</tei:label>
                            <tei:desc>Transcription Tool</tei:desc>
                        </tei:application>
                    </tei:appInfo>
                    <tei:transcriptionDesc ident="TGDP" version="2023">
                        <tei:desc><!--Fill me in--></tei:desc>
                        <tei:label><!--Fill me in--></tei:label>
                    </tei:transcriptionDesc>

                </tei:encodingDesc>

                <tei:revisionDesc>
                    <tei:change when="2023-04-11T08:19:27.000">Created by XSL transformation from an ELAN transcription</tei:change>
                </tei:revisionDesc>
            </tei:teiHeader>

            <!-- END TEI HEADER -->

            <tei:text xml:lang="de">
                <xsl:apply-templates select="//TIME_ORDER"/>
                <tei:body>
                    <!-- <xsl:apply-templates select="//TIER[@LINGUISTIC_TYPE_REF=$PRIMARY_TIER_TYPE]/ANNOTATION" mode="primary"> -->
                    <xsl:apply-templates select="//TIER[not(@PARENT_REF)]/ANNOTATION" mode="primary">
                        <xsl:sort select="exmaralda:TIME_SLOT_POSITION(ALIGNABLE_ANNOTATION/@TIME_SLOT_REF1)" data-type="number"/>
                    </xsl:apply-templates>
                </tei:body>
            </tei:text>
        </tei:TEI>

    </xsl:template>

    <xsl:template match="TIME_ORDER">
        <!-- ***************** -->
        <!-- Timeline, see 5.1 -->
        <!-- ***************** -->
        <tei:timeline unit="s">
            <tei:when xml:id="ts_origin" since="ts_origin" interval="0.0"/>
            <xsl:apply-templates select="TIME_SLOT"/>
            <tei:when xml:id="ts_end" since="ts_origin" interval="200000.0"/>
        </tei:timeline>
    </xsl:template>

    <!--  <TIME_SLOT TIME_SLOT_ID="ts1" TIME_VALUE="970"/> -->
    <xsl:template match="TIME_SLOT">
        <tei:when since="ts_origin">
            <xsl:attribute name="interval" select="@TIME_VALUE div 1000.0"/>
            <xsl:attribute name="xml:id" select="@TIME_SLOT_ID"/>
        </tei:when>
    </xsl:template>
    
    <xsl:template match="ANNOTATION" mode="primary">
        <tei:annotationBlock>
            <xsl:attribute name="start" select="ALIGNABLE_ANNOTATION/@TIME_SLOT_REF1"/>
            <xsl:attribute name="end" select="ALIGNABLE_ANNOTATION/@TIME_SLOT_REF2"/>
            <xsl:attribute name="who" select="ancestor::TIER/@PARTICIPANT"/>
            <!-- <xsl:attribute name="who" select="translate(ancestor::TIER/@TIER_ID, ' ', '_')"/> -->
            <xsl:attribute name="xml:id">
                <xsl:text>ab_</xsl:text><xsl:value-of select="ALIGNABLE_ANNOTATION/@ANNOTATION_ID"/>
            </xsl:attribute> 
            <xsl:variable name="THIS_ID" select="ALIGNABLE_ANNOTATION/@ANNOTATION_ID"/>
            <xsl:variable name="THIS_TIER_ID" select="ancestor::TIER/@TIER_ID"/>
            <xsl:variable name="THIS_START" select="ALIGNABLE_ANNOTATION/@TIME_SLOT_REF1"/>
            <xsl:variable name="THIS_END" select="ALIGNABLE_ANNOTATION/@TIME_SLOT_REF2"/>
            <xsl:variable name="THIS_POSITION" select="count(preceding-sibling::ANNOTATION) + 1"/>
            <tei:u>
                <xsl:attribute name="xml:id" select="$THIS_ID"/>
                <!-- <xsl:value-of select="ALIGNABLE_ANNOTATION/ANNOTATION_VALUE"/> -->
                <!-- words --> 
                <xsl:variable name="REPLACED1" select="replace(replace(ALIGNABLE_ANNOTATION/ANNOTATION_VALUE, '\[?\.\.\.\]?', '&#x2026;'),'&#x00A0;' ,' ')" />
                <xsl:variable name="REPLACED2" select="normalize-space(replace($REPLACED1, '\[\?{1,3}( \?{1,3})*\]', '(???)'))" />
                <xsl:analyze-string select="$REPLACED2" regex="([A-ZÄÖÜa-zäöüß]+(-)?|(\((\?)+)\))">
                    <xsl:matching-substring>
                        <tei:w>
                            <xsl:attribute name="xml:id"><xsl:value-of select="$THIS_ID"/><xsl:text>_w</xsl:text><xsl:value-of select="position()"/></xsl:attribute>
                            <xsl:value-of select="."/>
                        </tei:w>
                    </xsl:matching-substring>
                    <xsl:non-matching-substring>
                        <xsl:variable name="POSITION1" select="position()"/>
                        <xsl:analyze-string select="." regex=" +">
                            <xsl:matching-substring><!-- do nothing, i.e. get rid of whitespace --></xsl:matching-substring>
                            <!-- puncutation -->
                            <xsl:non-matching-substring>
                                <xsl:analyze-string select="." regex=".">
                                   <xsl:matching-substring>
                                       <xsl:variable name="POSITION2" select="position()"/>
                                       <tei:pc>
                                        <xsl:variable name="DUMMY_NODE">
                                                    <node/>
                                        </xsl:variable>
                                        <xsl:attribute name="xml:id">
                                               <xsl:value-of select="$THIS_ID"/>
                                               <xsl:text>_p</xsl:text>
                                               <xsl:value-of select="$POSITION1"/>
                                               <xsl:text>_</xsl:text>
                                               <xsl:value-of select="$POSITION2"/>
                                               <xsl:text>_</xsl:text>
                                               <xsl:value-of select="generate-id($DUMMY_NODE//node)"/> 
                                        </xsl:attribute>
                                        <xsl:value-of select="."/>
                                       </tei:pc>                                         
                                   </xsl:matching-substring>                                   
                                </xsl:analyze-string>
                            </xsl:non-matching-substring>
                        </xsl:analyze-string>
                    </xsl:non-matching-substring>
                </xsl:analyze-string>
            </tei:u>
            
            <xsl:choose>
                <xsl:when test="//REF_ANNOTATION[@ANNOTATION_REF=$THIS_ID and contains(ancestor::TIER/@TIER_ID, 'TRANSLATION')]">
                    <tei:spanGrp type="translation">
                        <tei:span>
                            <xsl:attribute name="from" select="$THIS_START"/>
                            <xsl:attribute name="to" select="$THIS_END"/>
                            <xsl:value-of select="//REF_ANNOTATION[@ANNOTATION_REF=$THIS_ID  and contains(ancestor::TIER/@TIER_ID, 'TRANSLATION')]/ANNOTATION_VALUE"/>
                        </tei:span>
                    </tei:spanGrp>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="//TIER[@PARENT_REF=$THIS_TIER_ID and starts-with(@TIER_ID, 'TRANSLATION')]">
                        <tei:spanGrp type="translation">
                            <tei:span>
                                <xsl:attribute name="from" select="$THIS_START"/>
                                <xsl:attribute name="to" select="$THIS_END"/>
                                <xsl:value-of select="//TIER[@PARENT_REF=$THIS_TIER_ID and contains(@TIER_ID, 'TRANSLATION')]/descendant::ANNOTATION[$THIS_POSITION]/descendant::ALIGNABLE_ANNOTATION"/>
                            </tei:span>
                        </tei:spanGrp>                        
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
            
            <tei:spanGrp type="original">
                <tei:span>
                    <xsl:attribute name="from" select="$THIS_START"/>
                    <xsl:attribute name="to" select="$THIS_END"/>
                    <xsl:value-of select="ALIGNABLE_ANNOTATION/ANNOTATION_VALUE"/>
                </tei:span>
                
            </tei:spanGrp>
            
            
        </tei:annotationBlock>
    </xsl:template>

</xsl:stylesheet>
