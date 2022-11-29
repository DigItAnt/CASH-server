import os, sys
import os.path
from conllu import parse
from conllu.exceptions import ParseException
import argparse
import requests
from colorama import Fore, Back, Style

KEY_PFIX = "conllu_"

def print_log(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)

def get_token(kc, username, password):
    data = {
        'client_id': 'princlient',
        'username': 'test',
        'password': 'testtest',
        'grant_type': 'password',
    }

    response = requests.post(kc, data=data)
    if response.status_code != 200:
        print_log(Fore.RED + "Error while getting token:" + Style.RESET_ALL,
                  response.text)
        sys.exit(response.status_code)
    return response.json()['access_token']

def create_file(filename, content, text, destination=0, token="", url=""):
    if url[-1] != '/':
        url += '/'
        
    headers = {
       'Authorization': f"Bearer {token}",
        'accept': '*/*',
    }

    params = {
        'requestUUID': '1',
        'element-id': destination,
    }

    files = {
        'file': (filename, content),
    }

    response = None
    try:   
        response = requests.post(f'{url}api/crud/uploadFile', params=params, headers=headers, files=files, verify=False)
    except Exception as e:
        print_log(Fore.RED + "Error while uploading file:",
                  str(e) + Style.RESET_ALL)
        sys.exit(1)
    
    if response.status_code != 200:
        print_log(Fore.RED + "Error creating file:" + Style.RESET_ALL,
                  response.text)
        sys.exit(response.status_code)

    elid = response.json()['node']['element-id']

    params = {
        'requestUUID': '1',
        'nodeid': elid
    }
    data = {
      "unstructured": {
        'conllu': text,
      } 
    }
    response = requests.post(f'{url}api/v1/unstructured', params=params, headers=headers, json=data, verify=False)
    if response.status_code != 200:
        print_log(Style.RED  + "Error uploading text:" + Style.RESET_ALL,
                  response.text, Style.RED + "remove node" + Style.RESET_ALL, elid)
        sys.exit(response.status_code)
    print_log(Fore.GREEN + "File created,", len(content),
              "bytes, id:" + Style.RESET_ALL, elid)
    return (elid, response.json()['unstructuredids']['conllu'])

def create_token(docid, token_text, position, span_from, span_to, token="", url=""):
    if url[-1] != '/':
        url += '/'
        
    headers = {
        'Authorization': f"Bearer {token}",
        'accept': '*/*',
    }

    params = {
        'requestUUID': '1',
        'nodeid': docid,
    }
    
    data = {
        'text': token_text,
        'xmlid': None,
        'position': position,
        'source': 'conllu',
        'begin': span_from,
        'end': span_to,
        'imported': True
    }
    
    response = requests.post(f'{url}api/v1/token', params=params, headers=headers, json=data, verify=False)
    if response.status_code != 200:
        print_log(Fore.RED + "Error creating token:" + Style.RESET_ALL,
                  response.text)
        sys.exit(response.status_code)
    #print_log(response.json())
    return response.json()['token']['id']

def create_annotation(docid, span_from, span_to, layer, value, attributes={}, token="", url=""):
    if url[-1] != '/':
        url += '/'
        
    headers = {
        'Authorization': f"Bearer {token}",
        'accept': '*/*',
    }

    params = {
        'requestUUID': '1',
        'nodeid': docid,
    }
    
    data = {
        'layer': layer,
        'value': value,
        'attributes': attributes,
        'spans': [ {'start': span_from, 'end': span_to} ]
    }
    
    response = requests.post(f'{url}api/v1/annotation', params=params, headers=headers, json=data, verify=False)
    #print_log(data)
    #print_log(response.json())
    if response.status_code != 200:
        print_log(Fore.RED + "Error creating annotation:" + Style.RESET_ALL,
                  data,
                  response.text)
        print_log(response)
        print_log(response.status_code)
        sys.exit(response.status_code)
    return response.json()['annotation']['id']   

#find the first occurrence of target in sentence starting from offset
def find_first(target, sentence, offset=0):
    return sentence.find(target, offset)

# reconstruct a sentence from a list of tokens
def reconstruct_sentence(tokens, offset=0):
    sentence = ""
    multiword_heads = {}
    text = tokens.metadata.get("text", None)
    int_offset = 0;
    for token in tokens:
        tid = token["id"]
        if not isinstance(tid, int): # token is a multiword token
            # extract x and y from a string in the form "x-y"
            x, _, y = tid
            multiword_heads[x] = token
            multiword_heads[y] = token

        if tid not in multiword_heads:
            if not text:
                space_after = " " if (token["misc"] or {}).get("SpaceAfter", "Yes") == "Yes" else ""
                sentence += token['form'] + space_after
                token['__span__from'] = offset + len(sentence)
                token['__span__to'] = offset + len(sentence) + len(token['form'])
            else: # have text, find first
                token['__span__from'] = offset + find_first(token['form'], text, int_offset)
                token['__span__to'] = token['__span__from'] + len(token['form'])
                int_offset = token['__span__to'] - offset
        else:
            token['__skip__'] = True
            token['__span__from'] = multiword_heads[tid]['__span__from']
            token['__span__to'] = multiword_heads[tid]['__span__to']
            
    ret = text or sentence.strip()
    print_log(Fore.GREEN + "Sentence:" + Style.RESET_ALL, ret)
    return ret

def parse_conllu_file(filename):
    data = []
    # use conllu to read a CoNLL-U file
    try:
        with open(filename, "r", encoding="utf-8") as f:
            text = f.read()
            try:
                data = parse(text)
            except ParseException as e:
                print_log(Fore.RED + "Error while parsing:", str(e) + Style.RESET_ALL)
                sys.exit(1)
    except IOError as e:
                print_log(Fore.RED + "I/O error while reading file:", str(e) + Style.RESET_ALL)
                sys.exit(1)
    return data# reconstruct sentences from tokens

def feat2str(feat):
    if not feat:
        return ""
    return "|".join([k + "=" + v for k, v in feat.items()])

def insert(filename, destination=0, kc_token="", url="", fields=None, sentence_layer="sentences",
           verbose=False, test=False):
    print_log(Fore.GREEN + "Parsing file:" + Style.RESET_ALL, filename)
    data = parse_conllu_file(filename)
    raw = ""
    with open(filename, "r", encoding="utf-8") as f:
        raw = f.read()
    
    text = ""
    for sentence in data:
        text += reconstruct_sentence(sentence, offset=len(text)) + "\n"
    
    # insert the text into CASH
    print_log(Fore.GREEN + "Inserting doc into " + url + ":" + Style.RESET_ALL +
              "\n", text[:50] + "...")
    docid, srcid = create_file(os.path.basename(filename), raw, text, destination, kc_token, url) if not test else (0, 0)

    tokencount = 1
    for sentence in data:
        sentence_start = -1
        sentence_end = -1
        for token in sentence:
            span_from = token['__span__from']
            span_to = token['__span__to']
            if sentence_start == -1:
                sentence_start = span_from
            sentence_end = span_to
            ann_cnt = 0
            for coli, col in enumerate(['form', 'lemma', 'upos', 'xpos',
                                        'feats', 'head', 'deprel', 'deps', 'misc']):
                if col == 'form': # insert the token
                    print_log(Fore.GREEN + "Inserting token:" + Style.RESET_ALL,
                              token[col] + Fore.GREEN + "@" +
                              str(span_from) + ":" + str(span_to), end = '')
                    if '__skip__' in token: tokencount -= 1
                    tokenid = create_token(docid, token[col], tokencount,
                                           span_from, span_to, kc_token, url) if not test else 0
                    tokencount += 1
                elif col == 'feats' or col == 'misc': # insert the features
                    layer = fields[coli-1] if fields else col
                    value = feat2str(token[col])
                    if value != '' and value is not None and (value != '_' or token['form'] == '_'):
                        if verbose: print_log("\nannotation:", layer, Style.RESET_ALL,
                                              value, Fore.GREEN, end='')
                        if not test:
                            create_annotation(docid, span_from, span_to,
                                              layer, value, token[col], kc_token, url)
                        ann_cnt += 1
                else: # insert the annotation
                    layer = fields[coli-1] if fields else col
                    value = token[col]
                    if value != '' and value is not None and (value != '_' or token['form'] == '_'):
                        if verbose: print_log("\nannotation:", layer, Style.RESET_ALL, value, Fore.GREEN, end='')
                        if not test:
                            create_annotation(docid, span_from, span_to,
                                              layer, value, {}, kc_token, url)
                        ann_cnt += 1
            if verbose: print_log(Style.RESET_ALL)
            else: print_log(" with", ann_cnt, "annotations" + Style.RESET_ALL)

        # insert the sentence annotation
        layer = sentence_layer
        value = "sentence"
        if not test:
            create_annotation(docid, sentence_start, sentence_end,
                              layer, value, sentence.metadata, kc_token, url)
        print_log(Fore.BLUE + Style.BRIGHT + "Inserting sentence annotation:" + Style.RESET_ALL,
                  sentence_start, sentence_end)
    return docid
                    
if __name__ == "__main__":
    argparser = argparse.ArgumentParser()
    argparser.add_argument("filename", help="CoNLL-U file to import")
    argparser.add_argument("-f", "--fields",
                           help="comma-separated list of column fields after the form " +
                           "(8 entries, default: lemma,upos,xpos,feats,head,deprel,deps,misc)",
                           default=None)
    argparser.add_argument("-s", "--sentence-layer", help="name of the sentence layer (default: sentences)",
                           default="sentences")
    argparser.add_argument("-d", "--destination", default=0, help="destination folder ID")
    argparser.add_argument("-u", "--username", help="username")
    argparser.add_argument("-p", "--password", help="password")    
    argparser.add_argument("-c", "--cash-url", default="https://lari2.ilc.cnr.it/cash/",
                           help="CASH URL")
    argparser.add_argument("-k", "--keycloak-url",
                           default="https://lari2.ilc.cnr.it/auth/realms/princnr/protocol/openid-connect/token",
                           help="CASH URL")
    argparser.add_argument("-v", "--verbose", help="Verbose mode (log more)", action="store_true", default=False)
    argparser.add_argument("--dry", help="Dry run, do not insert", action="store_true", default=False)
    args = argparser.parse_args()

    fields = None
    if ( args.fields ):
        fields = [f.strip() for f in args.fields.split(",")]
        if len(fields) != 8:
            print_log(Fore.RED + "Fields must be 8 comma-separated values for " +
                      "lemma,upos,xpos,feats,head,deprel,deps,misc" + Style.RESET_ALL)
            sys.exit(1)

    try:
        token = get_token(args.keycloak_url, args.username, args.password)
    except Exception as e:
        print_log(Fore.RED + "Error while getting OAuth token:", str(e) + Style.RESET_ALL)
        sys.exit(1)
            
    did = insert(args.filename, destination=args.destination, kc_token=token,
                 url=args.cash_url, fields=args.fields,
                 sentence_layer=args.sentence_layer,
                 verbose=args.verbose, test=args.dry)
    print(did)
