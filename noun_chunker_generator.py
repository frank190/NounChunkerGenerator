'''
Author: Leizhen Shi
Project Name: Noun Chunker Generator
The purpose of this project is to generate .tchunk and .pos files for the distributional ranking of the Termolator
The input should be cleaned file processed by Brandies Chinese Segemeter
'''

import argparse
import os

def load_directory(directory_name):
    return os.listdir(directory_name)


def process_data(filename):
    file = open(filename, 'r', encoding='UTF-8')
    result = []
    for line in file:
        line_in_list = line.split(' ') # change the line into a list by space
        for pair in line_in_list:
            splited_pair = pair.split('_') # change 本_DT to [‘本’, DT]
            result.append([splited_pair[0], splited_pair[1]])
    return result #result is a list of pairs with information word at first followed by the information

#If anyone wants to change the rules for detecting the term, this method is a good way to start
def detect_noun(pair_list):
    result = [] # information stored in the format of [word, tag, BIO_tag]
    for i in range (len(pair_list)):
        if is_noun(pair_list[i][1]): # detect the noun first
            if i > 0 and is_inword(result[i - 1][2]): # check if the previous word is in_word
                result.append([pair_list[i][0], pair_list[i][1], 'I-NP'])
            else: # if this noun is the start, then it will be in_word
                result.append([pair_list[i][0], pair_list[i][1], 'B-NP'])
        elif is_adj(pair_list[i][1]): # if an adj is detected, then it will be the start of the term
            result.append([pair_list[i][0], pair_list[i][1], 'I-NP'])
        else:
            result.append([pair_list[i][0], pair_list[i][1], 0])
    return result

def print_tchunk(filename, tagged_words):
    file = open(filename, 'w', encoding= "UTF-8")
    for tagged_word in tagged_words: # follow the CONLL format
        file.write(tagged_word[0]+"\t"+tagged_word[0]+"\t"+tagged_word[1]+"\t"+str(tagged_word[2])+"\n")
    file.close()

def print_pos(filename, tagged_words):
    start_point = 1
    file = open(filename, 'w', encoding= "UTF-8")
    for tagged_word in tagged_words:
        end_point = start_point + len(tagged_word[0])
        file.write(tagged_word[0] + "\t|||\tS: " + str(start_point) + "E: " + str(end_point) + "\t|||\t" + tagged_word[1] + "\n")
        start_point = end_point
    file.close()

def is_inword(BIOtag):
    inword_tag_set = set(['B', 'I'])
    return True if BIOtag in inword_tag_set else False

def is_adj(tag):
    adj_tag_set = set(["JJ", "JJS", "JJR"])
    return True if tag in adj_tag_set else False

def is_noun(tag):
    noun_tag_set = set(["NN", "NP"])
    return True if tag in noun_tag_set else False

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Outputting .tchunk and .pos files. A list of the tchunk names will be provided as well.")
    parser.add_argument('-f', '--foreground', nargs = 1, help = "Please enter the input foreground directory", required = True)
    parser.add_argument('-b', '--background', nargs = 1, help = "Please enter the output background directory", required = True)

    args = parser.parse_args()

    foreground_files = load_directory(args.foreground[0])
    background_files = load_directory(args.background[0])

    out_foreground_path = os.path.join(os.getcwd(), "output_foreground")
    out_background_path = os.path.join(os.getcwd(), "output_background")
    if not os.path.exists(out_foreground_path):
        os.mkdir(out_foreground_path)
    if not os.path.exists(out_background_path):
        os.mkdir(out_background_path)
    #output files from foreground
    for file in foreground_files:
        out_tchunk_file = "./output_foreground/" + file + ".tchunk"
        out_pos_file = "./output_foreground/" + file + ".pos"
        processed_data = process_data(args.foreground[0] + '/' + file)
        tagged_nouns = detect_noun(processed_data)
        print_tchunk(out_tchunk_file, tagged_nouns)
        print_pos(out_pos_file, tagged_nouns)

        to_write = open("foreground_tchunk_list", 'a+')
        to_write.write(out_tchunk_file + '\n')
        to_write.close()

    for file in background_files:
        out_tchunk_file = "./output_background/" + file + ".tchunk"
        out_pos_file = "./output_background/" + file + ".pos"
        processed_data = process_data(args.background[0] + '/' + file)
        tagged_nouns = detect_noun(processed_data)
        print_tchunk(out_tchunk_file, tagged_nouns)
        print_pos(out_pos_file, tagged_nouns)

        to_write = open("background_tchunk_list", 'a+')
        to_write.write(out_tchunk_file + '\n')
        to_write.close()