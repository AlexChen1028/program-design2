#!/usr/bin/env python3

import subprocess
import os
import sys
import shutil
import argparse
import time
import platform


CORPUSES = [
    "corpus0.txt",
    # "corpus1.txt",
    # "corpus2.txt",
]
TESTCASE_DICT = [
    {"testcase": "tc0.txt", "answer": "ans0.txt", "corpus": "corpus0"},
    {"testcase": "tc1.txt", "answer": "ans1.txt", "corpus": "corpus0"},
    {"testcase": "tc2.txt", "answer": "ans2.txt", "corpus": "corpus0"},
    {"testcase": "tc3.txt", "answer": "ans3.txt", "corpus": "corpus0"},
    {"testcase": "tc4.txt", "answer": "ans4.txt", "corpus": "corpus0"},
]
SDIFF_ARGS = ["sdiff", "--ignore-trailing-space", "-w", "120", "-l"]
if platform.system() == "Darwin":
    SDIFF_ARGS = ["sdiff", "-b", "-w", "120", "-l"]

current_dir = os.getcwd()
DIFF_DIR = os.path.join(current_dir, "diff")

if all(
    os.path.exists(os.path.join(current_dir, entry["testcase"]))
    for entry in TESTCASE_DICT
):
    base_path = current_dir
else:
    base_path = "/home/share/hw5"

CORPUSES = [os.path.join(base_path, file) for file in CORPUSES]
for entry in TESTCASE_DICT:
    entry["testcase"] = os.path.join(base_path, entry["testcase"])
    entry["answer"] = os.path.join(base_path, entry["answer"])


def build_java():
    if not os.path.exists("Indexer.java"):
        print("❌找不到 Indexer.java")
        exit(1)
    if not os.path.exists("BuildIndex.java"):
        print("❌找不到 TFIDFCalculator.java")
        exit(1)
    if not os.path.exists("TFIDFSearch.java"):
        print("❌找不到 TFIDFSearch.java")
        exit(1)
    try:
        subprocess.run(["javac", "Indexer.java", "BuildIndex.java", "TFIDFSearch.java"])
    except Exception as e:
        print(f"❌編譯時發生錯誤: {e}")
        exit(1)


def run_build_index(corpus):
    try:
        if not os.path.exists(corpus):
            print(f"❌找不到 {corpus}")
            exit(1)
        if not os.path.exists("BuildIndex.class"):
            print(f"❌找不到 BuildIndex.class 請先編譯")
            exit(1)

        subprocess.run(["java", "BuildIndex", corpus])
        print(f"✅測試資料集 {corpus} 產生 index 檔案成功")
    except Exception as e:
        print(f"❌執行程式時發生錯誤: {e}")
        exit(1)


def run_testcase(tc_number, corpus_name, testcase, answer_file):
    try:
        # 先清空之前產生過的檔案
        output_file_name = "output.txt"
        if os.path.exists(output_file_name):
            subprocess.run(["rm", output_file_name])

        start = time.time()
        subprocess.run(["java", "TFIDFSearch", corpus_name, testcase], timeout=60 * 3)
        end = time.time()
        elapsed = end - start
        print(f"執行時間: {elapsed:.2f} 秒")
        TESTCASE_DICT[tc_number]["time"] = elapsed
        output_file_name = "output.txt"
        if not os.path.exists(output_file_name):
            print(f"❌找不到 {output_file_name}")

        file_name = os.path.basename(answer_file)
        diff_process = subprocess.run(
            SDIFF_ARGS + [output_file_name, answer_file], text=True, capture_output=True
        )
        if diff_process.returncode == 0:
            print(f"{file_name}: ✅")
        else:
            # Save user's output
            output_path = os.path.join(f"diff/testcase{tc_number}", "output.txt")
            if not os.path.exists(os.path.dirname(output_path)):
                os.makedirs(os.path.dirname(output_path))
            shutil.copy(output_file_name, output_path)

            # Save output of diff to file
            diff_log = os.path.join(
                f"diff/testcase{tc_number}", answer_file.split("/")[-1] + ".diff"
            )
            if not os.path.exists(os.path.dirname(diff_log)):
                os.makedirs(os.path.dirname(diff_log))
            with open(diff_log, "w") as f:
                f.write(diff_process.stdout)

            # Save git diff output to file
            git_diff_process = subprocess.run(
                [
                    "git",
                    "diff",
                    "--ignore-space-at-eol",
                    "--color-words",
                    output_file_name,
                    answer_file,
                ],
                text=True,
                capture_output=True,
            )
            git_diff_log = os.path.join(
                f"diff/testcase{tc_number}", answer_file.split("/")[-1] + ".gitdiff"
            )
            if not os.path.exists(os.path.dirname(git_diff_log)):
                os.makedirs(os.path.dirname(git_diff_log))
            with open(git_diff_log, "w") as f:
                f.write(git_diff_process.stdout)

            print(f"{file_name}: ❌\t| 輸出結果已存至 '{output_path}'")
            print(f"\t\t| 對比結果已存至 '{diff_log}', '{git_diff_log}'")
            print(
                f"""\t\t| 請使用
            \t| vim {diff_log}\t(僅能對比每行前幾個字)
            \t| 或
            \t| less -R {git_diff_log}\t(按 q 退出、上下左右鍵移動，紅色代表你的輸出錯誤的地方，綠色為正確答案)"""
            )

    except FileNotFoundError as e:
        print("找不到 sdiff 或 git diff 命令")
    except subprocess.TimeoutExpired:
        print("❌執行超時")
        sys.exit(1)  # 終止程式


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--build_index",
        action=argparse.BooleanOptionalAction,
        default=True,
        required=False,
        help="Build index for all corpus",
    )
    parser.add_argument(
        "--testcase",
        required=False,
        type=int,
        help="Run specific testcase",
    )
    opt = parser.parse_args()
    # Remove files in DIFF_DIR
    if os.path.exists(DIFF_DIR):
        shutil.rmtree(DIFF_DIR)
    os.mkdir(DIFF_DIR)
    build_java()
    if opt.build_index:
        for i, corpus in enumerate(CORPUSES):
            print(f"corpus{i}: ", end="")
            run_build_index(corpus)
    if opt.testcase is not None:
        if 0 <= opt.testcase < len(TESTCASE_DICT):
            print(f"testcase{opt.testcase}: ", end="")
            run_testcase(
                opt.testcase,
                TESTCASE_DICT[opt.testcase]["corpus"],
                TESTCASE_DICT[opt.testcase]["testcase"],
                TESTCASE_DICT[opt.testcase]["answer"],
            )
        else:
            print("❌錯誤：無效的測試案例編號")
            exit(1)
    else:
        for i, entry in enumerate(TESTCASE_DICT):
            print(f"testcase{i}: ", end="")
            run_testcase(i, entry["corpus"], entry["testcase"], entry["answer"])

        total_time = sum(entry["time"] for entry in TESTCASE_DICT)
        mean_time = total_time / len(TESTCASE_DICT)
        print(f"平均執行時間: {mean_time:.2f} 秒")

