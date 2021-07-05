import gzip
import subprocess
import tempfile
import uuid
import logging
from pathlib import Path
from timeit import default_timer as timer
import sys

import pandas as pd
import gemmi

"""DIR_PATH = os.path.dirname(os.path.realpath(__file__))

ROOT_PATH = os.path.dirname(os.path.dirname(DIR_PATH))
RNAPDBEE_DIR = os.path.join(ROOT_PATH, "rnapdbee")
RNAPDBEE_PATH = os.path.join(RNAPDBEE_DIR, "rnapdbee.standalone-2.1-SNAPSHOT-jar-with-dependencies.jar")"""

main_logger = logging.getLogger('preprocessing')

class RnaspiderUserError(Exception):
    pass


class RnapdbeeError(Exception):
    pass


def check_config_errors(config_str: str) -> str:
    if not config_str:
        return "No config"

    try:
        config = json.loads(config_str)
    except:
        return "Wrong config format. Please send valid JSON"

    required_config_items = set(CONFIG_CHECK_CRITERIA.keys())
    config_items = set(key for key in config.keys() if not key.isspace())

    # if not required_config_items.issuperset(config_items):
    #     return f"Not all required configurations in the json: {', '.join(list(required_config_items - config_items))}"
    for item in CONFIG_CHECK_CRITERIA:
        value = config.get(item)
        to_check = CONFIG_CHECK_CRITERIA.get(item)
        i_type = to_check.get("type")
        if not i_type:
            continue
        if not isinstance(value, i_type):
            return f"{item} has to be type of {i_type}."
        if i_type == int:
            if value < to_check.get("min"):
                return f"{item} has to be more than {to_check.get('min')}."
            if value > to_check.get("max"):
                return f"{item} has to be no bigger than {to_check.get('max')}."
        poss = to_check.get("poss")
        if poss:
            if to_check.get("split"):
                l_values = value.split(to_check.get("split-separator", " "))
            else:
                l_values = [value]
            for value_ in l_values:
                if isinstance(value_, str) and to_check.get("split-type") == "int" and not value_.isnumeric():
                    return f"{item} values has to be integer."
                if value_ not in poss:
                    return f"{item} values has to be in {poss}. {value_} entered."
    return ""


def validate_structure2d_dot(model: bin) -> bool:
    """
    Check if 2d model is proper
    :param model: 2d dot-bracket model
    :type model: bin
    """
    model = model.decode()
    if not model:
        return False
    lines = model.splitlines()
    if len(lines) < 3:
        print("less then 3")
        return False
    if lines[0][0] != ">":
        print("wrong 1 line")
        return False
    for char in lines[1]:
        if char.upper() not in "ACGU;N":
            print("wrong second line", f"/{char}/")
            return False
    pairs = "\n".join(lines[2:])
    mapper = defaultdict(lambda: 0)
    opening = "([{<ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    closing = ")]}>abcdefghijklmnopqrstuvwxyz"
    for char in pairs:
        if char in opening:
            mapper[opening.index(char)] += 1
        elif char in closing:
            mapper[closing.index(char)] -= 1
        elif char not in ".;-":
            print("wrong other char", f"/{char}/")
            return False
    return all([x == 0 for x in mapper.values()])


def get_rna_nonstandard_residues_dict():
    non_standard_residues_A = ['A23', 'A2L', 'A2M', 'A39', 'A3P', 'A44', 'A5O', 'A6A', 'A7E', 'A9Z',
                               'ADI', 'ADP', 'AET', 'AMD', 'AMO', 'AP7', 'AVC', 'MA6', 'MAD', 'MGQ',
                               'MIA', 'MTU', 'M7A', '26A', '2MA', '6IA', '6MA', '6MC', '6MP', '6MT',
                               '6MZ', '6NW', 'F3N', 'N79', 'RIA', 'V3L', 'ZAD', '31H', '31M', '7AT',
                               'O2Z', 'SRA', '00A', '45A', '8AN', 'LCA', 'P5P', 'PPU', 'PR5', 'PU',
                               'T6A', 'TBN', 'TXD', 'TXP', '12A', '1MA', '5FA']

    non_standard_residues_G = ['A6G', 'E6G', 'E7G', 'EQ4', 'IG', 'IMP', 'M2G', 'MGT', 'MGV', 'MHG',
                               'QUO', 'YG', 'YYG', '23G', '2EG', '2MG', '2SG', 'B8K', 'B8W', 'B9B',
                               'BGH', 'N6G', 'RFJ', 'ZGU', '7MG', 'CG1', 'G1G', 'G25', 'G2L', 'G46',
                               'G48', 'G7M', 'GAO', 'GDO', 'GDP', 'GH3', 'GNG', 'GOM', 'GRB', 'GTP',
                               'KAG', 'KAK', 'O2G', 'OMG', '8AA', '8OS', 'LG', 'PGP', 'P7G', 'TPG',
                               'TG', 'XTS', '102', '18M', '1MG']

    non_standard_residues_C = ['A5M', 'A6C', 'E3C', 'IC', 'M4C', 'M5M', '6OO', 'B8Q', 'B8T', 'B9H',
                               'JMH', 'N5M', 'RPC', 'RSP', 'RSQ', 'ZBC', 'ZCY', '73W', 'C25', 'C2L',
                               'C31', 'C43', 'C5L', 'CBV', 'CCC', 'CH', 'CSF', 'OMC', 'S4C', '4OC',
                               'LC', 'LHH', 'LV2', 'PMT', 'TC', '10C', '1SC', '5HM', '5IC', '5MC']

    non_standard_residues_U = ['A6U', 'IU', 'I4U', 'MEP', 'MNU', 'U25', 'U2L', 'U2P', 'U31', 'U34',
                               'U36', 'U37', 'U8U', 'UAR', 'UBB', 'UBD', 'UD5', 'UPV', 'UR3', 'URD',
                               'US5', 'UZR', 'UMO', 'U23', '2AU', '2MU', '2OM', 'B8H', 'FHU', 'FNU',
                               'F2T', 'RUS', 'ZBU', '3AU', '3ME', '3MU', '3TD', '70U', '75B', 'CNU',
                               'OMU', 'ONE', 'S4U', 'SSU', 'SUR', '4SU', '85Y', 'DHU', 'H2U', 'LHU',
                               'PSU', 'PYO', 'P4U', 'T31', '125', '126', '127', '1RN', '5BU', '5FU',
                               '5MU', '9QV']
    dict_non_standard_residues_A = {residue: 'A' for residue in non_standard_residues_A}
    dict_non_standard_residues_C = {residue: 'C' for residue in non_standard_residues_C}
    dict_non_standard_residues_G = {residue: 'G' for residue in non_standard_residues_G}
    dict_non_standard_residues_U = {residue: 'U' for residue in non_standard_residues_U}
    return {**dict_non_standard_residues_A,
            **dict_non_standard_residues_C,
            **dict_non_standard_residues_G,
            **dict_non_standard_residues_U}


def nonstandard_residue_heur(residue_name):
    acgu = [nuc for nuc in residue_name if nuc in "ACGU"]
    return acgu[0] if len(acgu) == 1 else None


def prevalidate_structure3d(struct_bin: bytes, file_ext: str, logger):
    if file_ext.lower() != '.pdb':
        return struct_bin

    struct_str = struct_bin.decode()
    lines = struct_str.splitlines()
    for i, line in enumerate(lines):
        if line.startswith('MODEL'):
            model_line = line.split()
            lines[i] = f"{model_line[0]:<6}{model_line[1]:>7}"
    if lines[-1].startswith('ATOM') or lines[-1].startswith('HETATM'):
        num = int(lines[-1][6:11])
        lines.append(f'TER   {num+1:>5}\n')
    else:
        reversed_lines = lines[::-1]
        for reversed_line_no, (line, prev_line) in enumerate(zip(reversed_lines, reversed_lines[1:])):
            if prev_line.startswith('ATOM') or prev_line.startswith('HETATM'):
                if not line.startswith('ATOM') and not line.startswith('HETATM') and not line.startswith('TER'):
                    num = int(prev_line[6:11])
                    lines.insert(len(lines) - reversed_line_no - 1, f'TER   {num+1>5}\n')
                    break
    content = '\n'.join(lines)
    with open('./test-data', 'w') as output_file:
        output_file.write(content)
    return content.encode()


def parse_structure3d(struct_str, filename, logger):
    t_start = timer()
    output_filename = filename
    if filename.endswith('.gz'):
        struct_str = gzip.decompress(struct_str)
        output_filename = filename[:-3]

    if output_filename.endswith('.cif'):
        cif_block = gemmi.cif.read_string(struct_str)[0]
        output_string = gemmi.make_structure_from_block(cif_block)
    elif output_filename.endswith('.pdb'):
        output_string = gemmi.read_pdb_string(struct_str)
    else:
        # Should never reach here
        raise RnaspiderUserError('Unknown file format was provided.')

    t_end = timer()
    logger.info(f"Finished parsing input. Elapsed time: {int((t_end - t_start) * 1000)} ms")
    return output_string


def preprocess_structure3d(structure3d, dict_nonstandard_residues, split_policy, logger):
    t_start = timer()
    default_rna_residues = ['A', 'C', 'G', 'U']
    required_atoms = ['P', 'O5\'', 'C5\'', 'C4\'', 'C3\'', 'O3\'', 'O4\'', 'C1\'', 'N1', 'N3']
    required_atoms_dict = {
        'A': ['N6', 'N9'],
        'U': ['O2', 'O4'],
        'G': ['O6', 'N2'],
        'C': ['N4', 'N3', 'O2']
    }

    structure3d.assign_label_seq_id()
    structure3d.remove_alternative_conformations()
    structure3d.remove_hydrogens()
    structure3d.remove_waters()
    structure3d.remove_ligands_and_waters()
    structure3d.remove_empty_chains()

    warning_msgs = []
    nonrna_chain_count = 0

    # Prepare indices lists from first model
    for model in structure3d:
        rna_chains_names = []
        logger.info(f"Processing model {model.name}. Model length: {len(model)}.")
        chains_to_remove = []
        for chain_index, chain in enumerate(model):
            chain_type = chain.get_polymer().check_polymer_type()
            logger.info(f"Processing chain {chain.name}: {chain_type}.")
            if chain_type not in (gemmi.PolymerType.Rna, gemmi.PolymerType.DnaRnaHybrid):
                if chain_length := len(chain) < 10 and chain_type == gemmi.PolymerType.Unknown:
                    logger.warning(f"Could not recognise type of chain {chain.name} due to short length "
                                   f"({chain_length} residue{'s' if chain_length > 1 else ''}).")
                else:
                    chains_to_remove.append(chain_index)
                    nonrna_chain_count += 1
                    continue
            rna_chains_names.append(chain.name)

            residues_to_remove = []
            for residue_index, residue in enumerate(chain):
                removal_reason = None
                # handle Inosine and DNA separately, because otherwise it will be mapped to ACGU,
                if residue.name in ["I", "N", "DA", "DI", "DC", "DG", "DU", "DT", "T"]:
                    warn_msg = f"Abnormal residue {residue.name} {residue.seqid} from chain {chain.name} " \
                               f"removed from the input data (unknown residue type)."
                    warning_msgs.append(warn_msg)
                    residues_to_remove.append(residue_index)
                    continue
                orig_name = residue.name
                if residue.name not in default_rna_residues:
                    residue_name = dict_nonstandard_residues.get(residue.name) or nonstandard_residue_heur(residue.name)
                    if not residue_name:
                        warn_msg = f"Abnormal residue {residue.name} {residue.seqid} from chain {chain.name} " \
                                   f"removed from the input data (unknown residue type)."
                        warning_msgs.append(warn_msg)
                        residues_to_remove.append(residue_index)
                        continue

                    residue.name = residue_name
                    residue.het_flag = "A"
                    warn_msg = f"Modified residue {orig_name} {residue.seqid} from chain {chain.name} changed to {residue_name}."
                    warning_msgs.append(warn_msg)

                for atom in required_atoms:
                    if residue_index == 0 and atom == "P":
                        continue
                    if atom not in residue:
                        removal_reason = f"atom {atom} missing"
                        break
                for atom in required_atoms_dict[residue.name]:
                    if atom not in residue:
                        removal_reason = f"atom {atom} missing"
                        break

                if removal_reason:
                    warn_msg = f"Residue {orig_name} {residue.seqid} from chain {chain.name} removed from the input data " \
                               f"({removal_reason})."
                    warning_msgs.append(warn_msg)
                    residues_to_remove.append(residue_index)

            #tylko dla loga
            if len(residues_to_remove) > 0:
                warn_msg = f"{len(residues_to_remove)} of {len(chain)} removed from chain {chain.name}."
                warning_msgs.append(warn_msg)

            # if it's rather not RNA (RNA/DNA hybrid or DNA with ribonucleotides), drop chain
            if 2 * len(residues_to_remove) > len(chain):
                warn_msg = f"Incomplete chain {chain.name} removed from the input data (>50% residues missing)."
                warning_msgs.append(warn_msg)
                chains_to_remove.append(chain_index)
                continue

            for residue_index in reversed(residues_to_remove):
                del chain[residue_index]

        for chain_index in reversed(chains_to_remove):
            del model[chain_index]

        if nonrna_chain_count > 0:
            warn_msg = f"{nonrna_chain_count} non-RNA chain{'s' if nonrna_chain_count > 1 else ''} removed " \
                       f"from input data. The following chains passed for processing: {rna_chains_names}."
            warning_msgs.append(warn_msg)

        if len(model) == 0:
            raise RnaspiderUserError("No RNA strands available for processing.")

    t_end = timer()
    logger.info(f"Finished preprocessing. Elapsed time: {int((t_end - t_start) * 1000)} ms.")
    return warning_msgs


def get_structure_stem(path):
    filename = Path(path).stem
    # handle .cif.gz
    if filename.endswith(".cif") or filename.endswith(".pdb"):
        return filename[:-4]
    return filename


def structure_to_string(structure, stem, output_extension):
    if output_extension.lower() == '.cif':
        document = gemmi.cif.Document()
        block = document.add_new_block(f"{stem}_rnaonly_standarized")
        # set all group flags to be written to false
        groups = gemmi.MmcifOutputGroups(False)
        # write only atom group
        groups.atoms = True
        structure.update_mmcif_block(block, groups)
        table = block.find_mmcif_category("_atom_site_anisotrop")
        if table is not None:
            table.erase()
        return document.as_string()
    elif output_extension.lower() == '.pdb':
        return structure.make_minimal_pdb()

#tworzy kopie struktury i usuwa niepotrzebne modele poza jednym
def split_3d_structure(structure: gemmi.Structure):
    structures_3d_splitted = []
    for model_index in range(len(structure)):
        structure_copy = structure.clone()
        del structure_copy[(model_index + 1):]
        del structure_copy[:model_index]
        structures_3d_splitted.append(structure_copy)
    return structures_3d_splitted

"""
def generate_structure2d(structure3d_path, logger):
    t_start = timer()
    absolute_path = structure3d_path.absolute()
    structures3d_dir = absolute_path.parent
    try:
        result = subprocess.run(
            ['java', '-jar', f'{RNAPDBEE_PATH}', f'-c{RNAPDBEE_DIR}/config.properties', f'-i{absolute_path}',
             f'-o{structures3d_dir}', '-aDSSR', '-dVARNA', '-pHYBRID'],
            cwd=RNAPDBEE_DIR, capture_output=True, text=True)
    except Exception as e:
        logger.error(f'Error occurred when using RNApdbee: {e}')
        raise RnapdbeeError()
    else:
        if result.stderr and result.stderr != '':
            logger.error(f'Error occurred when using RNApdbee: {result.stderr}')
            raise RnapdbeeError()
    structure2d_strands = Path(structures3d_dir / "0/strands.dbn").read_text()
    structure2d_svg = Path(structures3d_dir / "0/structure.svg").read_text()
    t_end = timer()
    logger.info(f"Finished secondary structure generation. Elapsed time: {int((t_end - t_start) * 1000)} ms.")
    return structure2d_strands, structure2d_svg


def find_gap_indices(structure):
    subchains_gaps = []
    for subchain in structure[0]:
        subchain_gaps = [0]
        for index, (residue1, residue2) in enumerate(zip(subchain, subchain[1:])):
            if 'O3\'' in residue1 and 'O5\'' in residue2:
                distance = residue1.find_atom('O3\'', '*').pos.dist(residue2.find_atom('O5\'', '*').pos)
                if distance > 9:
                    subchain_gaps.append(index)
        subchains_gaps.append(subchain_gaps)
    return subchains_gaps


def postprocess_structure2d(gap_indices, structure2d_strands, logger):
    t_start = timer()
    structure2d_strands_arr = structure2d_strands.split('\n')[:-1]
    acgu_chains = structure2d_strands_arr[1::3]
    dot_chains = structure2d_strands_arr[2::3]
    if len(acgu_chains) == 0 or len(dot_chains) == 0:
        raise ValueError(f"Error occurred when postprocessing secondary structure: sequence was empty.")
    acgu_gapped_chains = []
    dot_gapped_chains = []
    for gap_indices, acgu_chain, dot_chain in zip(gap_indices, acgu_chains, dot_chains):
        acgu_parts = [acgu_chain[i:j] for i, j in zip(gap_indices, gap_indices[1:] + [None])]
        acgu_gapped_chains.append("n".join(acgu_parts))
        dot_parts = [dot_chain[i:j] for i, j in zip(gap_indices, gap_indices[1:] + [None])]
        dot_gapped_chains.append("-".join(dot_parts))

    long_acgu_string = ";".join(acgu_gapped_chains) + ";"
    long_dot_string = ";".join(dot_gapped_chains) + ";"

    t_end = timer()
    logger.info(f"Finished secondary structure postprocessing. Elapsed time: {int((t_end - t_start) * 1000)} ms.")
    return "\n".join([">strand", long_acgu_string, long_dot_string])


def process_secondary_data(request_id: str, task_nr: int, structure3d_path: Path, structure3d_orig_filename: str,
                           structure3d_gap_indices, logger):
    _, structure3d_extension = get_file_extensions(structure3d_orig_filename)

    update_task_stage(request_id, task_nr, Stage.GENERATING_2D)
    logger_local = logger.getChild(f'[{structure3d_orig_filename}] - RNApdbee processing')
    structure2d_strands, structure2d_svg = generate_structure2d(structure3d_path, logger_local)

    logger_local = logger.getChild(f'[{structure3d_orig_filename}] - DOT postprocessing')
    structure2d_dot = postprocess_structure2d(structure3d_gap_indices, structure2d_strands, logger_local)

    structure3d_string = structure3d_path.read_text()
    update_task_add_2d(
        request_id=request_id,
        task_nr=task_nr,
        model_2d=structure2d_strands.encode(),
        model_2d_svg=structure2d_svg.encode()
    )

    logger.info(f"Queued task for engine processing.")
    update_task_stage(request_id, task_nr, Stage.QUEUED)
    # send task to the rabbitmq
    config = get_config_for_request(request_id=request_id)
    sender = Send(host=os.environ.get("RABBITMQ_HOST"), queue_name="submit_tasks")
    sender.send(task_id=f"{request_id}#{task_nr}", model_3d=structure3d_string.encode(),
                model_3d_extension=structure3d_extension[1:], model_2d=structure2d_dot.encode(), config=config,
                logger=logger)
"""

def preprocess_structure(structure3d_orig: bin, structure3d_orig_filename: str,
                         split_policy: str, out_path: str):#request_id: str, task_nr: int, 
    """
    Thread behavior to send task to the engine if all files are properly formatted
    :param request_id: request id in database
    :type request_id: str
    :param task_nr: task number of request
    :type task_nr: int
    :param structure3d_orig: provided 3D model from user, encoded
    :param structure3d_orig_filename: filename to get extension from
    :param split_policy: declares how to analyze 3D model, if there are multiple models in file.
           Possible values: "all" | "first"
    """
    """app = create_app()
    with app.app_context():"""
    main_logger.info(f"Start processing {structure3d_orig_filename}.")
    structure3d_orig_ext, structure3d_output_ext = get_file_extensions(structure3d_orig_filename)

    with tempfile.TemporaryDirectory(prefix="RNApony_cif_files") as tmpdir:#prefix=request_id
        Path(out_path).mkdir(parents=True, exist_ok=True)
        processing_tmp_path = Path(tmpdir)
        stem = get_structure_stem(structure3d_orig_filename)
        dict_rna_nonstandard_residues = get_rna_nonstandard_residues_dict()

        #try:
        logger = main_logger.getChild(f"[{structure3d_orig_filename}] - parsing input")
        #update_task_stage(request_id, task_nr, Stage.PARSING)
        #Dodaje na koncu TER w plikach .pdb w cif nie trzeba
        structure3d_validated_str = prevalidate_structure3d(structure3d_orig, structure3d_output_ext, logger)
        #wczytuje cif lub pdb
        structure3d_orig = parse_structure3d(structure3d_validated_str, structure3d_orig_filename, logger)
        warn_messages = []

        logger = main_logger.getChild(f"[{structure3d_orig_filename}] - preprocessing")
        #update_task_stage(request_id, task_nr, Stage.SEPARATING)
        if split_policy == "first":
            if len(structure3d_orig) > 1:
                del structure3d_orig[1:]
                warn_msg = "Model 1 processed, the others discarded."
                warn_messages.append([warn_msg])
            else:
                warn_messages.append([])
            structures_splitted = [structure3d_orig]
        elif split_policy == "all":
            warn_messages = [[] for _ in range(len(structure3d_orig))]
            structures_splitted = split_3d_structure(structure3d_orig)

        #update_task_stage(request_id, task_nr, Stage.FILTERING)
        for task_warn_messages, structure_splitted in zip(warn_messages, structures_splitted):
            preproc_warn_messages = preprocess_structure3d(structure_splitted, dict_rna_nonstandard_residues,
                                                            split_policy, logger)
            task_warn_messages.extend(preproc_warn_messages)
        # structures_splitted_strings to poprawny plik .cif lub .pdb
        structures_splitted_strings = [structure_to_string(structure_splitted, stem, structure3d_output_ext)
                                        for structure_splitted in structures_splitted]
        # structures_splitted_names to nazwa pliku
        structures_splitted_names = [f"{stem}_model{structure[0].name}{structure3d_output_ext}" for
                                        structure in
                                        structures_splitted]
        """#baza danych
        if split_policy == "first":
            update_task_model_3d(request_id=request_id, task_nr=task_nr,
                                    model_3d=structures_splitted_strings[0].encode())
            task_nrs = [task_nr]
        elif split_policy == "all":
            task_nrs = split_task(request_id, task_nr, structures_splitted_strings,
                                    structures_splitted_names)
        else:
            raise RnaspiderUserError("Unknown model split policy specified.")"""

        #tworzy ścieżkę do pliku
        """structures3d_paths = [Path(processing_tmp_path / structure3d_filename) for structure3d_filename in
                                structures_splitted_names]"""
        structures3d_paths = [Path(Path(out_path) / structure3d_filename) for structure3d_filename in
                                structures_splitted_names]
        for structure3d_path, structures_splitted_string in zip(structures3d_paths,
                                                                structures_splitted_strings):
            print("saving")
            structure3d_path.write_text(structures_splitted_string)

        #dla plików .dot
        """all_gap_indices = [find_gap_indices(structure) for structure in structures_splitted]
        # with multiprocessing.Pool(processes=min(len(task_nrs), multiprocessing.cpu_count())) as pool:
        #     pool.starmap(process_secondary_data,
        #                  zip(repeat(request_id), task_nrs, structures3d_paths,
        #                      repeat(structure3d_orig_filename),
        #                      all_gap_indices, repeat(logger)))
        # app.logger.info(f"Lists lengths: {len(task_nrs)} {len(structures3d_paths)} {len(all_gap_indices)} {len(warn_messages)}")
        for task_nr, structure3d_path, gap_indices, task_warn_messages in zip(task_nrs,
                                                                                structures3d_paths,
                                                                                all_gap_indices,
                                                                                warn_messages):
            process_secondary_data(request_id, task_nr, structure3d_path, structure3d_orig_filename,
                                    gap_indices, logger)
            # for task_nr, task_warn_messages in zip(task_nrs, warn_messages):
            warnings_string = "\n".join(filter(None, task_warn_messages))
            update_task_warnings(request_id, task_nr, warnings_string)
            update_task_stage(request_id, task_nr, Stage.FINISHED_ANALYSIS)"""

        """
        except RnaspiderUserError as e:
            update_task(
                request_id=request_id,
                task_nr=task_nr,
                success=False,
                stage=Stage.FINISHED_ANALYSIS,
                message=str(e)
            )
        except RnapdbeeError as e:
            update_task(
                request_id=request_id,
                task_nr=task_nr,
                success=False,
                stage=Stage.FINISHED_ANALYSIS,
                message="Couldn't generate secondary model using RNApdbee."
            )
        except Exception as e:
            app.logger.critical(e)
            update_task(
                request_id=request_id,
                task_nr=task_nr,
                success=False,
                stage=Stage.FINISHED_ANALYSIS,
                message="Unknown error occurred during analysis."
            )"""

def get_file_extensions(filename):
    extensions = Path(filename).suffixes
    if extensions[-1] != ".gz":
        return extensions[-1].lower(), extensions[-1].lower()
    else:
        return "".join(extensions[-2:]).lower(), extensions[-2].lower()


def check_allowed_extension(extension):
    allowed_extensions = [".pdb", ".cif", ".mmcif", ".pdb.gz", ".cif.gz", ".mmcif.gz"]
    return extension in allowed_extensions


def generate_request_id() -> str:
    """
    Generate unique request id using uuid1 method because if's impossible to duplicate values
    https://docs.python.org/3/library/uuid.html#uuid.uuid1
    :rtype str
    """
    return uuid.uuid1().__str__()


if __name__ == "__main__":
    filename = sys.argv[1]
    split_policy = "all"
    f = open(filename, "r")
    structure3d_orig = f.read()
    preprocess_structure(structure3d_orig.encode(), filename, split_policy, sys.argv[2])