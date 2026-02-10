"""
Text Processing Service
Handles text cleaning, language detection, tokenization, and entity extraction
"""

import logging
import re
from typing import List, Dict, Any, Optional

import spacy
from langdetect import detect, DetectorFactory

logger = logging.getLogger(__name__)

# Set seed for reproducible language detection
DetectorFactory.seed = 42


class TextProcessor:
    """Service for processing and analyzing text"""
    
    def __init__(self):
        """Initialize the text processor"""
        logger.info("Initializing text processor")
        
        # Load spaCy models (will be loaded on demand)
        self.nlp_models = {}
        
        # Common stopwords
        self.stopwords = self._load_stopwords()
        
        logger.info("Text processor initialized")
    
    def _load_stopwords(self) -> set:
        """Load common stopwords for FR, EN, AR"""
        # French stopwords
        fr_stopwords = {
            'le', 'la', 'les', 'un', 'une', 'des', 'et', 'en', 'de', 'du', 'à', 'au',
            'aux', 'par', 'pour', 'dans', 'sur', 'avec', 'sans', 'sous', 'est', 'sont',
            'ce', 'ces', 'cet', 'cette', 'il', 'elle', 'ils', 'elles', 'je', 'tu',
            'nous', 'vous', 'son', 'sa', 'ses', 'leur', 'leurs', 'mon', 'ma', 'mes',
            'ton', 'ta', 'tes', 'notre', 'nos', 'votre', 'vos', 'que', 'qui', 'quoi',
            'dont', 'où', 'quand', 'comment', 'pourquoi', 'car', 'mais', 'ou', 'donc',
            'ni', 'or', 'ne', 'pas', 'plus', 'moins', 'très', 'trop', 'peu', 'beaucoup',
            'tout', 'tous', 'toute', 'toutes', 'autre', 'autres', 'même', 'tel', 'telle'
        }
        
        # English stopwords
        en_stopwords = {
            'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
            'of', 'with', 'by', 'from', 'as', 'is', 'was', 'are', 'were', 'be',
            'been', 'being', 'have', 'has', 'had', 'do', 'does', 'did', 'will',
            'would', 'could', 'should', 'may', 'might', 'must', 'can', 'this',
            'that', 'these', 'those', 'i', 'you', 'he', 'she', 'it', 'we', 'they',
            'me', 'him', 'her', 'us', 'them', 'my', 'your', 'his', 'her', 'its',
            'our', 'their', 'mine', 'yours', 'hers', 'ours', 'theirs', 'what',
            'which', 'who', 'when', 'where', 'why', 'how', 'all', 'any', 'both',
            'each', 'few', 'more', 'most', 'other', 'some', 'such', 'no', 'nor',
            'not', 'only', 'own', 'same', 'so', 'than', 'too', 'very', 'just'
        }
        
        return fr_stopwords.union(en_stopwords)
    
    def detect_language(self, text: str) -> str:
        """
        Detect the language of the text
        
        Args:
            text: Input text
            
        Returns:
            Language code (en, fr, ar, or unknown)
        """
        if not text or len(text.strip()) < 10:
            return "unknown"
        
        try:
            # Check for Arabic characters first
            if self._contains_arabic(text):
                return "ar"
            
            # Use langdetect for other languages
            detected = detect(text)
            
            if detected in ['en', 'fr']:
                return detected
            
            return "unknown"
            
        except Exception as e:
            logger.warning(f"Language detection failed: {e}")
            return "unknown"
    
    def _contains_arabic(self, text: str) -> bool:
        """Check if text contains Arabic characters"""
        arabic_pattern = re.compile(r'[\u0600-\u06FF\u0750-\u077F\u08A0-\u08FF]+')
        return bool(arabic_pattern.search(text))
    
    def clean_text(self, text: str) -> str:
        """
        Clean and normalize text
        
        Args:
            text: Raw input text
            
        Returns:
            Cleaned text
        """
        if not text:
            return ""
        
        # Convert to lowercase
        text = text.lower()
        
        # Remove URLs
        text = re.sub(r'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+', '', text)
        
        # Remove email addresses
        text = re.sub(r'\S+@\S+', '', text)
        
        # Remove HTML tags
        text = re.sub(r'<[^>]+>', '', text)
        
        # Remove special characters but keep accented characters for French
        text = re.sub(r'[^\w\s\u00C0-\u017F\u0600-\u06FF]', ' ', text)
        
        # Remove extra whitespace
        text = re.sub(r'\s+', ' ', text).strip()
        
        # Remove very short words (less than 2 characters)
        words = text.split()
        words = [w for w in words if len(w) >= 2]
        
        return ' '.join(words)
    
    def tokenize(self, text: str, language: str = "unknown") -> List[str]:
        """
        Tokenize text into words
        
        Args:
            text: Input text
            language: Language code
            
        Returns:
            List of tokens
        """
        if not text:
            return []
        
        # Simple whitespace tokenization
        tokens = text.lower().split()
        
        # Remove stopwords
        tokens = [t for t in tokens if t not in self.stopwords]
        
        return tokens
    
    def remove_stopwords(self, tokens: List[str]) -> List[str]:
        """
        Remove stopwords from token list
        
        Args:
            tokens: List of tokens
            
        Returns:
            Filtered tokens
        """
        return [t for t in tokens if t not in self.stopwords and len(t) >= 3]
    
    def extract_entities(self, text: str) -> List[Dict[str, Any]]:
        """
        Extract named entities from text (simplified)
        
        Args:
            text: Input text
            
        Returns:
            List of entities with type and text
        """
        entities = []
        
        # Simple pattern-based entity extraction
        # In production, use spaCy NER models
        
        # Extract locations (cities in Morocco)
        moroccan_cities = [
            'casablanca', 'rabat', 'marrakech', 'tanger', 'agadir', 'fes',
            'meknes', 'oujda', 'kenitra', 'tetouan', 'safi', 'mohammedia',
            'el jadida', 'beni mellal', 'nador', 'khemisset', 'taourirt'
        ]
        
        text_lower = text.lower()
        for city in moroccan_cities:
            if city in text_lower:
                entities.append({
                    "text": city.title(),
                    "type": "GPE",
                    "start": text_lower.find(city),
                    "end": text_lower.find(city) + len(city)
                })
        
        # Extract organizations (common tech/startup terms)
        org_patterns = [
            r'\b(\w+\s+)?(?:tech|technologies|solutions|systems|digital|startup|company)\b'
        ]
        
        for pattern in org_patterns:
            for match in re.finditer(pattern, text_lower):
                entities.append({
                    "text": match.group().title(),
                    "type": "ORG",
                    "start": match.start(),
                    "end": match.end()
                })
        
        return entities
    
    def extract_keywords(self, text: str, top_n: int = 10) -> List[str]:
        """
        Extract keywords from text using simple TF approach
        
        Args:
            text: Input text
            top_n: Number of top keywords to return
            
        Returns:
            List of keywords
        """
        tokens = self.tokenize(text)
        tokens = self.remove_stopwords(tokens)
        
        # Count frequencies
        freq = {}
        for token in tokens:
            freq[token] = freq.get(token, 0) + 1
        
        # Return top N
        sorted_tokens = sorted(freq.items(), key=lambda x: x[1], reverse=True)
        return [token for token, _ in sorted_tokens[:top_n]]
    
    def summarize(self, text: str, max_sentences: int = 3) -> str:
        """
        Generate a simple extractive summary
        
        Args:
            text: Input text
            max_sentences: Maximum number of sentences in summary
            
        Returns:
            Summary text
        """
        # Simple sentence splitting
        sentences = re.split(r'[.!?]+', text)
        sentences = [s.strip() for s in sentences if len(s.strip()) > 20]
        
        if not sentences:
            return text[:200] + "..." if len(text) > 200 else text
        
        # Return first N sentences
        summary = '. '.join(sentences[:max_sentences])
        return summary + '.' if not summary.endswith('.') else summary
